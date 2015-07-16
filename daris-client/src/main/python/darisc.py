'''
Created on 12 Jul 2015

@author: wliu5@unimelb.edu.au
'''
import os.path
import subprocess

class Session(object):
        
    def __init__(self, daris_client_jar=None, host=None, port=None, transport=None, token=None, sid=None):
        self.set_daris_client_jar(daris_client_jar)
        self.set_server(host, port, transport)
        self.set_token(token)
        self.set_sid(sid)
        
        
    def set_daris_client_jar(self, path):
        if path is not None and not os.path.isfile(path):
            raise ValueError('File: ' + self.__daris_client_jar + ' does not exist.')
        self.__daris_client_jar = path
    
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
        self.__check_java()
        self.__check_server()
        cmd = ['java', '-jar', self.__daris_client_jar]
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
        self.__check_java()
        self.__check_server()
        if not self.__token and not self.__sid:
            raise Exception('Not logged on. No sid or secure identity token is set.')
        cmd = ['java', '-jar', self.__daris_client_jar]
        cmd += ['-mf.host', self.__host]
        cmd += ['-mf.port', str(self.__port)]
        cmd += ['-mf.transport', self.__transport]
        if self.__token: # token has 1st priority
            cmd += ['-mf.token', self.__token]
        else:
            cmd += ['-mf.sid', self.__sid]
        cmd.append('execute')
        cmd.append('service')
    
    def __make_args(self, args, inputs, output):
        if args is None:
            pass
        
    
    def logoff(self):
        self.__check_java()
        self.__check_server()
        cmd = ['java', '-jar', self.__daris_client_jar]
        cmd += ['-mf.host', self.__host]
        cmd += ['-mf.port', str(self.__port)]
        cmd += ['-mf.transport', self.__transport]
        cmd.append('logoff')
        try:
            sp = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            (stdout, stderr) = sp.communicate()
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
        
    
    def __check_java(self):
        if not self.__daris_client_jar:
            raise Exception('daris client jar is not set.')
        if not os.path.isfile(self.__daris_client_jar):
            raise Excetpion('daris client jar file: ' + self.__daris_client_jar + ' does not exist.')
        try:
            sp = subprocess.Popen(["java", "-version"], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            (stdout, stderr) = sp.communicate()
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
    

if __name__ == '__main__':
    session = Session('/tmp/daris-client.jar', 'localhost', 8086, 'http')
    session.logon('system', 'manager', 'change_me')
    session.logoff()
    
    
    
    
