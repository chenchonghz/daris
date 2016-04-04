#!/usr/bin/env python

import datetime
import os
import os.path
import re
import time
import subprocess
import sys
import xml.etree.ElementTree as ElementTree

DARIS_CLIENT_JAR_PATH = None


def get_daris_client_jar_path():
    """ Returns the path to daris-client.jar.
    """
    global DARIS_CLIENT_JAR_PATH
    if not DARIS_CLIENT_JAR_PATH:
        try:
            DARIS_CLIENT_JAR_PATH = os.environ['DARIS_CLIENT_JAR_PATH']
        except KeyError:
            pass
    if not DARIS_CLIENT_JAR_PATH:
        path = os.path.dirname(__file__) + '/daris-client.jar'
        if os.path.isfile(path):
            DARIS_CLIENT_JAR_PATH = path
    return DARIS_CLIENT_JAR_PATH


def set_daris_client_jar_path(path):
    """ Sets the location of daris-client.jar.
    """
    global DARIS_CLIENT_JAR_PATH
    DARIS_CLIENT_JAR_PATH = path


def check_daris_client_jar():
    """ Checks if daris-client.jar is available.
    """
    path = get_daris_client_jar_path()
    if not path:
        raise Exception('DARIS_CLIENT_JAR_PATH is not set.')
    if not os.path.isfile(path):
        raise Exception('DaRIS client jar file: ' + path + ' does not exist.')


def check_java():
    """ Checks if java is available. 
    """
    try:
        sp = subprocess.Popen(["java", "-version"], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        (stdout, stderr) = sp.communicate()  # @UnusedVariable
        if not stderr or not stderr.startswith('java version'):
            raise Exception('Failed to parse java version. Found : ' + stderr)
        i1 = stderr.find('"')
        i2 = -1 if i1 == -1 else stderr.find('"', i1 + 1)
        if i1 < 0 or i2 < 0:
            raise Exception('Failed to parse java version. Found : ' + stderr)
        version = stderr[i1 + 1:i2]
        if not version:
            raise Exception('Failed to parse java version. Found : ' + stderr)
        jv = version.split('.')[1]
        if not jv:
            raise Exception('Failed to parse java version. Found : ' + stderr)
        if int(jv) < 7:
            raise Exception('Requires Java 7 and above. Found Java: ' + jv)
    except Exception:
        raise Exception('Java not found.')


def check_dependencies():
    """ Checks the dependencies(Java and daris-client.jar).
    """
    check_java()
    check_daris_client_jar()


def start_daris_client_command():
    """ Creates the command string for executing the daris-client.jar
    """
    basename = os.path.basename(__file__)
    return ['java', '-Ddc.prefix=' + basename, '-jar', get_daris_client_jar_path()]


def get_xml_header(version='1.0', encoding='UTF-8'):
    return '<?xml version="' + version + '" encoding="' + encoding + '"?>'


class XmlElement(object):
    def __init__(self, elem=None, name=None, attrib=None, value=None):
        if elem is not None:
            if name is not None:
                raise ValueError("Expecting 'elem' or 'name'. Found both.")
            if not isinstance(elem, ElementTree.Element):
                raise TypeError("'elem' must be an instance of xml.etree.ElementTree.Element.")
            self._elem = elem
        else:
            if name is None:
                raise ValueError("Expecting 'elem' or 'name'. Found none.")
            if attrib is None:
                attrib = {}
            else:
                if isinstance(attrib, dict):  # dictionary
                    attrib = {str(k): str(attrib[k]) for k in attrib.keys()}
                else:
                    raise ValueError("'attrib' must be an instance of dictionary.")
            idx = name.find(':')
            if idx >= 0:
                ns = name[0:idx]
                self._elem = ElementTree.Element('{' + ns + '}' + name[idx + 1:], attrib=attrib)
            else:
                self._elem = ElementTree.Element(name, attrib=attrib)
            if value is not None:
                self._elem.text = str(value)
        self._nsmap = {}
        self._register_namespace(self._elem)

    def _register_namespace(self, elem):
        tag = elem.tag
        if tag.startswith('{'):
            ns = tag[1:tag.rfind('}')]
            self._nsmap[ns] = ns
        for subelem in list(elem):
            self._register_namespace(subelem)

    @property
    def tag(self):
        if self._elem.tag.startswith('{'):
            idx = self._elem.tag.find('}')
            ns = self._elem.tag[1:idx]
            name = self._elem.tag[idx + 1:]
            return ns + ':' + name
        else:
            return self._elem.tag

    @property
    def attrib(self):
        return self._elem.attrib.copy()

    @property
    def text(self):
        return self._elem.text

    def name(self):
        return self.tag

    def attributes(self):
        return self.attrib

    def attribute(self, name):
        return self.attrib.get(name)

    def set_attribute(self, name, value):
        self.attrib.set(name, str(value))

    def value(self, xpath=None, default=None):
        if xpath is None:
            return self._elem.text
        else:
            idx = xpath.rfind('/@')
            if idx == -1:
                return self._elem.findtext(xpath, default=default, namespaces=self._nsmap)
            else:
                se = self._elem.find(xpath[:idx], namespaces=self._nsmap)
                if se is not None:
                    value = se.attrib.get(xpath[idx + 2:])
                    return value if value is not None else default

    def int_value(self, xpath=None, default=None, base=10):
        assert default is None or isinstance(default, int)
        value = self.value(xpath)
        if value is not None:
            return int(value, base)
        else:
            return default

    def float_value(self, xpath=None, default=None):
        assert default is None or isinstance(default, float)
        value = self.value(xpath)
        if value is not None:
            return float(value)
        else:
            return default

    def boolean_value(self, xpath=None, default=None):
        assert default is None or isinstance(default, bool)
        value = self.value(xpath)
        if value is not None:
            return value.lower() in ('yes', 'true', '1')
        else:
            return default

    def date_value(self, xpath=None, default=None):
        assert default is None or isinstance(default, datetime.datetime)
        value = self.value(xpath)
        if value is not None:
            return time.strptime(value, '%d-%b-%Y %H:%M:%S')
        else:
            return default

    def set_value(self, value):
        if value is not None:
            if isinstance(value, datetime.datetime):
                self._elem.text = time.strftime('%d-%b-%Y %H:%M:%S', value)
            elif isinstance(value, bool):
                self._elem.text = str(value).lower()
            else:
                self._elem.text = str(value)

    def values(self, xpath=None):
        if xpath is None:
            if self._elem.text is not None:
                return [self._elem.text]
            else:
                return None
        idx = xpath.rfind('/@')
        if idx == -1:
            ses = self._elem.findall(xpath, self._nsmap)
            if ses is not None:
                return [se.text for se in ses]
        else:
            ses = self._elem.findall(xpath[:idx], self._nsmap)
            if ses is not None:
                return [se.attrib.get(xpath[idx + 2:]) for se in ses]

    def element(self, xpath=None):
        if xpath is None:
            ses = list(self._elem)
            return XmlElement(elem=ses[0]) if ses else None
        else:
            idx = xpath.rfind('/@')
            if idx != -1:
                raise ValueError('Invalid element xpath: ' + xpath)
            se = self._elem.find(xpath, self._nsmap)
            if se is not None:
                return XmlElement(elem=se)

    def elements(self, xpath=None):
        if xpath is None:
            ses = list(self._elem)
            if ses:
                return [XmlElement(elem=se) for se in ses]
        else:
            idx = xpath.rfind('/@')
            if idx != -1:
                raise SyntaxError('invalid element xpath: ' + xpath)
            ses = self._elem.findall(xpath, self._nsmap)
            if ses:
                return [XmlElement(elem=se) for se in ses]
            else:
                return None

    def add_element(self, elem, index=None):
        assert elem is not None and isinstance(elem, (ElementTree.Element, XmlElement))
        if isinstance(elem, ElementTree.Element):
            if index is None:
                self._elem.append(elem)
            else:
                self._elem.insert(index, elem)
            self._register_namespace(elem)
        elif isinstance(elem, XmlElement):
            self.add_element(elem._elem, index)

    def tostring(self):
        for ns in self._nsmap.keys():
            ElementTree.register_namespace(ns, self._nsmap.get(ns))
        te = ElementTree.Element('temp')
        te.append(self._elem)
        ts = ElementTree.tostring(te)
        ts = ts[ts.find('>') + 1:len(ts) - 7]
        for nsk in self._nsmap.keys():
            nsv = self._nsmap.get(nsk)

            def replacement(match):
                token = match.group(0)
                if token.endswith(' '):  # ends with space
                    return token + 'xmlns:' + nsk + '="' + nsv + '" '
                else:  # ends with >
                    return token[0:-1] + ' xmlns:' + nsk + '="' + nsv + '">'

            ts = re.sub(r'<' + nsk + ':[a-zA-Z0-9_-]+[\s>]', replacement, ts)
        return ts

    def __str__(self):
        return self.tostring()

    def __getitem__(self, index):
        se = self._elem.__getitem__(index)
        return XmlElement(se) if se is not None else None

    def __len__(self):
        return self._elem.__len__()

    @classmethod
    def parse(cls, source):
        assert source is not None
        if os.path.isfile(source):  # text is a file
            tree = ElementTree.parse(source)
            if tree is not None:
                root = tree.getroot()
                if root is not None:
                    return XmlElement(elem=root)
            else:
                raise ValueError('Failed to parse XML file: ' + source)
        else:
            return XmlElement(ElementTree.fromstring(str(source)))


def _process_attributes(name, attributes):
    attrib = {}
    # add namespace attribute
    idx = name.find(':')
    if idx >= 0:
        ns = name[0:idx]
        ns_attr = 'xmlns:' + ns
        if ns_attr not in attrib:
            attrib[ns_attr] = ns
    # conver to str and remove attribute with value==None
    if attributes is not None:
        for name in attributes.keys():
            value = attributes[name]
            if value is not None:
                attrib[str(name)] = str(value)
    return attrib


class XmlStringWriter(object):
    def __init__(self, root=None):
        self.__stack = []
        self.__items = []
        if root is not None:
            self.push(str(root))

    def doc_text(self):
        self.pop_all()
        return ''.join(self.__items)

    def doc_elem(self):
        return XmlElement.parse(self.doc_text())

    def push(self, name, attributes=None):
        attributes = _process_attributes(name, attributes)
        self.__stack.append(name)
        self.__items.append('<')
        self.__items.append(name)
        for a in attributes.keys():
            self.__items.append(' ')
            self.__items.append(a)
            self.__items.append('="')
            self.__items.append(attributes[a])
            self.__items.append('"')
        self.__items.append('>')

    def pop(self):
        name = self.__stack.pop()
        self.__items.append('</')
        self.__items.append(name)
        self.__items.append('>')

    def pop_all(self):
        while len(self.__stack) > 0:
            self.pop()

    def add(self, name, value, attributes=None):
        attributes = _process_attributes(name, attributes)
        self.__items.append('<')
        self.__items.append(name)
        for a in attributes.keys():
            self.__items.append(' ')
            self.__items.append(a)
            self.__items.append('="')
            self.__items.append(attributes[a])
            self.__items.append('"')
        self.__items.append('>')
        self.__items.append(str(value))
        self.__items.append('</')
        self.__items.append(name)
        self.__items.append('>')

    def add_element(self, element, parent=True):
        if element is None:
            raise ValueError('element is not specified.')
        if isinstance(element, ElementTree.Element) or isinstance(element, XmlElement):
            if parent is True:
                if isinstance(element, ElementTree.Element):
                    self.__items.append(XmlElement(element).tostring())
                else:
                    self.__items.append(element.tostring())
            else:
                for sub_element in list(element):
                    self.add_element(sub_element, parent=True)
        else:
            elem = XmlElement.parse(str(element))
            self.add_element(elem, parent)


class XmlDocWriter(object):
    def __init__(self, root=None):
        self.__stack = []
        self.__tb = ElementTree.TreeBuilder()
        if root is not None:
            self.push(root)

    def doc_text(self):
        return str(self.doc_elem())

    def doc_elem(self):
        self.pop_all()
        return XmlElement(self.__tb.close())

    def push(self, name, attributes=None):
        attributes = _process_attributes(name, attributes)
        self.__stack.append(name)
        self.__tb.start(name, attributes)

    def pop(self):
        name = self.__stack.pop()
        if name is not None:
            self.__tb.end(name)

    def pop_all(self):
        while len(self.__stack) > 0:
            self.pop()

    def add(self, name, value, attributes=None):
        attributes = _process_attributes(name, attributes)
        self.__tb.start(name, attributes)
        self.__tb.data(str(value))
        self.__tb.end(name)

    def add_element(self, element, parent=True):
        if element is None:
            raise ValueError('element is not specified.')
        if isinstance(element, ElementTree.Element) or isinstance(element, XmlElement):
            if parent is True:
                self.__tb.start(element.tag, element.attrib)
                if element.text is not None:
                    self.__tb.data(element.text)
            for sub_element in list(element):
                self.add_element(sub_element, parent=True)
            if parent is True:
                self.__tb.end(element.tag)
        else:
            self.add_element(ElementTree.fromstring(str(element)), parent)


class Session(object):
    def __init__(self, host=None, port=None, transport=None, token=None, sid=None):
        check_dependencies()
        self.__host = host
        self.__port = port
        if transport is not None and transport not in ('http', 'https', 'tcp/ip', 'HTTP', 'HTTPS', 'TCP/IP'):
            raise ValueError('Invalid transport: ' + transport + '. Expects http, https or tcp/ip.')
        self.__transport = transport
        self.__token = token
        self.__sid = sid

    def set_server(self, host, port, transport):
        self.__host = host
        self.__port = port
        if transport is not None and transport not in ('http', 'https', 'tcp/ip', 'HTTP', 'HTTPS', 'TCP/IP'):
            raise ValueError('Invalid transport: ' + transport + '. Expects http, https or tcp/ip.')
        self.__transport = transport

    def set_token(self, token):
        self.__token = token

    def set_sid(self, sid):
        self.__sid = sid

    def logon(self, domain, user, password):
        self.__check_server()
        cmd = start_daris_client_command()
        cmd += ['--mf.host', self.__host]
        cmd += ['--mf.port', str(self.__port)]
        cmd += ['--mf.transport', self.__transport]
        cmd.append('logon')
        cmd.append(domain)
        cmd.append(user)
        cmd.append(password)
        sp = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        (stdout, stderr) = sp.communicate()
        if stderr:
            raise Exception('Failed to log on DaRIS(Mediaflux). \nError: ' + stderr)
        if stdout and not stderr:
            self.__sid = stdout.rstrip('\n')

    def execute(self, service, args=None, inputs=None, output=None):
        self.__check_server()
        if not self.__token and not self.__sid:
            raise Exception('Not logged on. No sid or secure identity token is set.')
        cmd = start_daris_client_command()
        cmd += ['--mf.host', self.__host]
        cmd += ['--mf.port', str(self.__port)]
        cmd += ['--mf.transport', self.__transport]
        cmd += ['--mf.output', 'xml']
        if self.__token:  # token has 1st priority
            cmd += ['--mf.token', self.__token]
        else:
            cmd += ['--mf.sid', self.__sid]
        cmd.append('execute')
        cmd.append(service)
        args = self.__build_args(args, inputs, output)
        if args is not None:
            cmd.append(args)
        sp = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        (stdout, stderr) = sp.communicate()
        if stderr:
            raise Exception('Failed to execute service: ' + service + ' on DaRIS(Mediaflux). \nError: ' + stderr)
        if stdout and not stderr:
            return XmlElement.parse(stdout.rstrip('\n'))

    @staticmethod
    def __build_args(args, inputs, output):
        if args is None:
            if (inputs is not None and len(inputs) > 0) or output is not None:
                w = XmlStringWriter('args')
                if inputs is not None:
                    for i in inputs:
                        w.add('in', i)
                if output is not None:
                    w.add('out', output)
                return w.doc_text()
            else:
                return None
        else:
            if isinstance(args, XmlElement):
                pass
            elif isinstance(args, ElementTree.Element):
                args = XmlElement(elem=args)
            else:
                args = str(args)
                if not args.startswith('<args>'):
                    args = '<args>' + args
                if not args.endswith('</args>'):
                    args += '</args>'
                args = XmlElement.parse(args)
            if args is not None:
                if (inputs is not None and len(inputs) > 0) or output is not None:
                    if inputs is not None:
                        for si in inputs:  # @ReservedAssignment
                            args.add_element(XmlElement(name='in', value=si))
                    if output is not None:
                        args.add_element(XmlElement(name='out', value=output))
                return str(args)
            else:
                return None

    def logoff(self):
        self.__check_server()
        cmd = start_daris_client_command()
        cmd += ['--mf.host', self.__host]
        cmd += ['--mf.port', str(self.__port)]
        cmd += ['--mf.transport', self.__transport]
        cmd.append('logoff')
        try:
            sp = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            (stdout, stderr) = sp.communicate()  # @UnusedVariable
            if stderr:
                raise Exception('Failed to log off DaRIS(Mediaflux). \nError: ' + stderr)
        finally:
            self.__sid = None

    def __check_server(self):
        if not self.__host:
            raise Exception('Server host is not set.')
        if not self.__port:
            raise Exception('Server port is not set.')
        if not self.__transport:
            raise Exception('Server transport is not set.')


class CID(object):
    @classmethod
    def get_parent_cid(cls, cid, level=1):
        pid = cid
        while level >= 1:
            idx = pid.rfind('.')
            if idx == -1:
                return None
            else:
                pid = pid[:idx]
            level -= 1
        return pid

    @classmethod
    def get_cid_depth(cls, cid):
        if cid is None or len(cid) == 0:
            return 0
        depth = 1
        idx = cid.find('.')
        while idx != -1:
            depth += 1
            idx = cid.find('.', idx + 1)
        return depth

    @classmethod
    def is_project_cid(cls, cid):
        return cls.get_cid_depth(cid) == 3

    @classmethod
    def is_subject_cid(cls, cid):
        return cls.get_cid_depth(cid) == 4

    @classmethod
    def is_ex_method_cid(cls, cid):
        return cls.get_cid_depth(cid) == 5

    @classmethod
    def is_study_cid(cls, cid):
        return cls.get_cid_depth(cid) == 6

    @classmethod
    def is_dataset_cid(cls, cid):
        return cls.get_cid_depth(cid) == 7

    @classmethod
    def get_project_cid(cls, cid):
        depth = cls.get_cid_depth(cid)
        if depth < 3:
            return None
        elif depth == 3:
            return cid
        else:
            return cls.get_parent_cid(cid, depth - 3)

    @classmethod
    def get_subject_cid(cls, cid):
        depth = cls.get_cid_depth(cid)
        if depth < 4:
            return None
        elif depth == 4:
            return cid
        else:
            return cls.get_parent_cid(cid, depth - 4)

    @classmethod
    def get_ex_method_cid(cls, cid):
        depth = cls.get_cid_depth(cid)
        if depth < 5:
            return None
        elif depth == 5:
            return cid
        else:
            return cls.get_parent_cid(cid, depth - 5)

    @classmethod
    def get_study_cid(cls, cid):
        depth = cls.get_cid_depth(cid)
        if depth < 6:
            return None
        elif depth == 6:
            return cid
        else:
            return cls.get_parent_cid(cid, depth - 6)

    @classmethod
    def get_ordinal(cls, cid):
        idx = cid.rfind('.')
        if idx == -1:
            return cid
        else:
            return cid[idx + 1:]


def main(argv):
    """ The main method wraps daris-client.jar. It checks availability of Java and daris-client.jar,
        executes daris-client.jar by passing the command arguments to it.
    """
    # check the dependencies: java and daris-client.jar
    check_dependencies()
    # start making the command line.
    cmd = start_daris_client_command()
    # append the arguments
    cmd += argv
    # execute the command line
    subprocess.call(cmd)


if __name__ == '__main__':
    # set_daris_client_jar_path('/path/to/daris-client.jar')
    main(sys.argv[1:])
