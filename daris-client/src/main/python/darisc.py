#!/usr/bin/env python

'''
Created on 12 Jul 2015

@author: wliu5@unimelb.edu.au
'''

import xml.etree.ElementTree as ElementTree
import time
import datetime
import os.path
import subprocess
import sys

DARIS_CLIENT_JAR_PATH = None

def get_daris_client_jar_path():
    """ Returns the path to daris-client.jar.
    """
    global DARIS_CLIENT_JAR_PATH
    if not DARIS_CLIENT_JAR_PATH:
        try:
            DARIS_CLIENT_JAR_PATH = os.environ['DARIS_CLIENT_JAR_PATH']
        except(KeyError):
            pass
    if not DARIS_CLIENT_JAR_PATH:
        path = os.path.dirname(__file__) + '/daris-client.jar'
        if os.path.isfile(path):
            DARIS_CLIENT_JAR_PATH = path
    return DARIS_CLIENT_JAR_PATH

def set_daris_client_jar_path(path):
    """ Sets the location of daris-client.jar.
    """
    global  DARIS_CLIENT_JAR_PATH
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
            raise Exception('Required Java 7. Found Java: ' + jv)
    except(Exception):
        raise Exception('Java not found.')

def check_dependencies():
    """ Checks the dependencies(Java and daris-client.jar).
    """
    check_java()
    check_daris_client_jar()

def create_daris_client_command():
    """ Creates the command string for executing the daris-client.jar
    """
    basename = os.path.basename(__file__)
    return  ['java', '-Ddc.prefix=' + basename, '-jar', get_daris_client_jar_path()]

class XmlElement(object):
    """ The class for Xml Element. It wraps ElementTree.Element."""
    def __init__(self, element=None, name=None, attributes=None, value=None):
        if element is not None:
            if name is not None:
                raise ValueError('Expecting element or name. Found both.')
            if not isinstance(element, ElementTree.Element):
                raise TypeError('element must be an instance of xml.etree.ElementTree.Element.')
            self._element = element
        else:
            if name is None:
                raise ValueError('Expecting element or name. Found none.')
            if attributes is None:
                attrib = {}
            else:
                if isinstance(attributes, dict):  # dictionary
                    attrib = {str(k): str(attributes[k]) for k in attributes.keys()}
                else:
                    raise ValueError('attributes must be an instance of dictionary.')
            self._element = ElementTree.Element(name, attrib=attrib)
            if value is not None:
                self._element.text = str(value)

    @property
    def tag(self):
        return self._element.tag

    @property
    def attrib(self):
        return self._element.attrib.copy()

    @property
    def text(self):
        return self._element.text

    def name(self):
        return self._element.tag

    def attributes(self):
        return self._element.attrib.copy()

    def attribute(self, name):
        return self._element.attrib.get(name)

    def set_attribute(self, name, value):
        self._element.attrib.set(name, str(value))

    def value(self, xpath=None):
        if xpath is None:
            return self._element.text
        else:
            idx = xpath.rfind('/@')
            if idx == -1:
                return self._element.findtext(xpath)
            else:
                se = self._element.find(xpath[:idx])
                return se.attrib.get(xpath[idx + 2:])

    def int_value(self, xpath=None, default=None):
        value = self.value(xpath)
        if value is not None:
            return int(value)
        else:
            if default is not None:
                return int(default)
    
    def float_value(self, xpath=None, default=None):
        value = self.value(xpath)
        if value is not None:
            return float(value)
        else:
            if default is not None:
                return float(default)

    def boolean_value(self, xpath=None, default=None):
        value = self.value(xpath)
        if value is None:
            if default is not None:
                if isinstance(default, bool):
                    return default
                else:
                    if(str(default).lower() in ('yes', 'true', '1',)):
                        return True
                    elif(str(default).lower() in ('no', 'false', '0')):
                        return False
                    else:
                        raise TypeError('Invalid default value: ' + default)
        else:
            return value.lower() in ('yes', 'true', '1')

    def date_value(self, xpath=None, default=None):
        value = self.value(xpath)
        if value is None:
            if default is not None:
                if isinstance(default, datetime.datetime):
                    return default
                else:
                    return time.strptime(str(default), '%d-%b-%Y %H:%M:%S') 
        else:
            return time.strptime(value, '%d-%b-%Y %H:%M:%S')

    def set_value(self, value):
        if value is not None:
            if isinstance(value, datetime.datetime):
                self._element.text = time.strftime('%d-%b-%Y %H:%M:%S', value)
            elif isinstance(value, bool):
                self._element.text = str(value).lower()
            else:
                self._element.text = str(value)

    def values(self, xpath=None):
        if xpath is None:
            if self._element.text is not None:
                return [self._element.text]
            else:
                return None
        idx = xpath.rfind('/@')
        if idx == -1:
            ses = self._element.findall(xpath)
            if ses is not None:
                return [se.text for se in ses]
        else:
            ses = self._element.findall(xpath[:idx])
            return [se.attrib.get(xpath[idx + 2:]) for se in ses]

    def element(self, xpath=None):
        if xpath is None:
            ses = list(self._element)
            return XmlElement(element=ses[0]) if ses else None
        else:
            idx = xpath.rfind('/@')
            if idx != -1:
                raise ValueError('Invalid element xpath: ' + xpath)
            se = self._element.find(xpath)
            if se is not None:
                return XmlElement(element=se)

    def elements(self, xpath=None):
        if xpath is None:
            ses = list(self._element)
            if ses:
                return [XmlElement(element=se) for se in ses]
        else:
            idx = xpath.rfind('/@')
            if idx != -1:
                raise SyntaxError('invalid element xpath: ' + xpath)
            ses = self._element.findall(xpath)
            if ses:
                return [XmlElement(element=se) for se in ses]
            else:
                return None

    def add_element(self, element, index=None):
        if element is None:
            raise ValueError('No element is specified.')
        if isinstance(element, ElementTree.Element):
            if index is None:
                self._element.append(element)
            else:
                self._element.insert(index, element)
        elif isinstance(element, XmlElement):
            self.add_element(element._element, index)
        else:
            raise TypeError('Invalid element type: ' + type(element))

    def tostring(self):
        return ElementTree.tostring(self._element)

    def __str__(self):
        return self.tostring()

    def __getitem__(self, index):
        se = self._element.__getitem__(index)
        return XmlElement(se) if se is not None else None

    def __len__(self):
        return self._element.__len__()

class XmlDoc(object):
    """ A class contains only a class method to parse a xml string or file to a XmlElement object.
    """
    @classmethod
    def parse(cls, text):
        if os.path.isfile(text):  # text is a file
            tree = ElementTree.parse(text)
            if tree is not None:
                return XmlElement(tree.getroot())
            else:
                raise ValueError('Failed to parse XML file: ' + text)
        else:
            return XmlElement(ElementTree.fromstring(str(text)))

class XmlStringWriter(object):
    """ A class to build a string in xml format.
    """
    def __init__(self, root=None):
        self.__stack = []
        self.__items = []
        if root is not None:
            self.push(str(root))

    def doc_text(self):
        self.pop_all()
        return ''.join(self.__items)

    def doc_elem(self):
        doc = XmlDoc.parse(self.doc_text())
        if doc is not None:
            return doc.root()

    def push(self, name, attributes=None):
        self.__stack.append(name)
        self.__items.append('<')
        self.__items.append(name)
        if attributes is not None:
            for a in attributes.keys():
                self.__add_attr(a, attributes[a])
        self.__items.append('>')

    def __add_attr(self, name, value):
        if name is not None and value is not None:
            if not value:
                self.__items.append(' ')
                self.__items.append(str(name))
                self.__items.append('="')
                self.__items.append(str(value))
                self.__items.append('"')

    def pop(self):
        name = self.__stack.pop()
        self.__items.append('</')
        self.__items.append(name)
        self.__items.append('>')

    def pop_all(self):
        while len(self.__stack) > 0:
            self.pop()

    def add(self, name, value, attributes=None):
        self.__items.append('<')
        self.__items.append(name)
        if attributes is not None:
            for a in attributes.keys():
                self.__add_attr(a, attributes[a])
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
                    self.__items.append(ElementTree.tostring(element))
                else:
                    self.__items.append(element.tostring())
            else:
                for sub_element in list(element):
                    self.add_element(sub_element, parent=True)
        else:
            if parent is True:
                self.__items.append(str(element))
            else:
                elem = ElementTree.fromstring(str(element))
                for sub_elem in list(elem):
                    self.add_element(sub_elem, parent=True)

class XmlDocWriter(object):
    """ A class to build xml document.
    """
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
        self.__stack.append(name)
        attrib = {}
        if attributes is not None:
            if isinstance(attributes, dict):
                attrib = {str(k): str(attributes[k]) for k in attributes.keys()}
            else:
                attrib = {a.name(): a.value() for a in attributes}
        self.__tb.start(name, attrib)

    def pop(self):
        name = self.__stack.pop()
        if name is not None:
            self.__tb.end(name)

    def pop_all(self):
        while len(self.__stack) > 0:
            self.pop()

    def add(self, name, value, attributes=None):
        attrib = {}
        if attributes is not None:
            attrib = {str(k): str(attributes[k]) for k in attributes.keys()}
        self.__tb.start(name, attrib)
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
        self.set_server(host, port, transport)
        self.set_token(token)
        self.set_sid(sid)
    
    def set_server(self, host, port, transport):
        self.__host = host
        self.__port = port
        if transport is not None and not transport in ('http', 'https', 'tcp/ip', 'HTTP', 'HTTPS', 'TCP/IP'):
            raise ValueError('Invalid transport: ' + transport + '. Expects http, https or tcp/ip.')
        self.__transport = transport
    
    def set_token(self, token):
        self.__token = token
    
    def set_sid(self, sid):
        self.__sid = sid
    
    def logon(self, domain, user, password):
        self.__check_server()
        cmd = ['java', '-Ddc.prefix=darisc.py', '-jar', get_daris_client_jar_path()]
        cmd += ['-mf.host', self.__host]
        cmd += ['-mf.port', str(self.__port)]
        cmd += ['-mf.transport', self.__transport]
        cmd.append('logon')
        cmd.append(domain)
        cmd.append(user)
        cmd.append(password)
        sp = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        (stdout, stderr) = sp.communicate()
        if stderr:
            raise Exception('Failed to log on DaRIS(Mediaflux). \nError: ' + stderr);
        if stdout and not stderr:
            self.__sid = stdout.rstrip('\n')
    
    def execute(self, service, args=None, inputs=None, output=None):
        self.__check_server()
        if not self.__token and not self.__sid:
            raise Exception('Not logged on. No sid or secure identity token is set.')
        cmd = ['java', '-Ddc.prefix=darisc.py', '-jar', DARIS_CLIENT_JAR_PATH]
        cmd += ['-mf.host', self.__host]
        cmd += ['-mf.port', str(self.__port)]
        cmd += ['-mf.transport', self.__transport]
        cmd += ['-mf.output', 'xml']
        if self.__token:  # token has 1st priority
            cmd += ['-mf.token', self.__token]
        else:
            cmd += ['-mf.sid', self.__sid]
        cmd.append('execute')
        cmd.append(service)
        args = self.__build_args(args, inputs, output)
        if args is not None:
            cmd.append(args)
        sp = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        (stdout, stderr) = sp.communicate()
        if stderr:
            raise Exception('Failed to execute service: ' + service + ' on DaRIS(Mediaflux). \nError: ' + stderr);
        if stdout and not stderr:
            return XmlDoc.parse(stdout.rstrip('\n'))
    
    def __build_args(self, args, inputs, output):
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
                args = XmlElement(element=args)
            else:
                args = str(args)
                if not args.startswith('<args>'):
                    args = '<args>' + args
                if not args.endswith('</args>'):
                    args = args + '</args>'
                args = XmlDoc.parse(args)
            if args is not None:
                if (inputs is not None and len(inputs) > 0) or output is not None:
                    if inputs is not None:
                        for input in inputs:  # @ReservedAssignment
                            args.add_element(XmlElement(name='in', value=input))
                    if output is not None:
                        args.add_element(XmlElement(name='out', value=output))
                return str(args)
            else:
                return None
    
    def logoff(self):
        self.__check_server()
        cmd = ['java', '-Ddc.prefix=darisc.py', '-jar', DARIS_CLIENT_JAR_PATH]
        cmd += ['-mf.host', self.__host]
        cmd += ['-mf.port', str(self.__port)]
        cmd += ['-mf.transport', self.__transport]
        cmd.append('logoff')
        try:
            sp = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            (stdout, stderr) = sp.communicate()  # @UnusedVariable
            if stderr:
                raise Exception('Failed to log off DaRIS(Mediaflux). \nError: ' + stderr);
        finally:
            self.__sid = None
    
    def __check_server(self):
        if not self.__host:
            raise Exception('Server host is not set.')
        if not self.__port:
            raise Exception('Server port is not set.')
        if not self.__transport:
            raise Exception('Server transport is not set.')

def main(argv):
    """ The main method wraps daris-client.jar. It checks availability of Java and daris-client.jar, executes daris-client.jar by passing the command arguments to it.
    """
    # check the dependencies: java and daris-client.jar
    check_dependencies()
    # start making the command line.
    cmd = create_daris_client_command()
    # append the arguments
    cmd += argv
    # execute the command line
    subprocess.call(cmd)
    
if __name__ == '__main__':
    # set_daris_client_jar_path('/path/to/daris-client.jar')
    main(sys.argv[1:])
 
    
