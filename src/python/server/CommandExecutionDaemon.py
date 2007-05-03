#!python
"""
CommandExecutionDaemon.py

Runs a limited set of commands on this machine, called remotely through XMLRPC.

"""

legalCommands = """
  dir
  cmd
  start
  python
  ping
  notepad
  ant
  type
""".split()

currentlyRunningCommands = {}

import sys, os, GetTrueIP, subprocess, time
from threading import Thread
from RequestServer import RequestServer
CommandExecutionDaemonServerXMLRPCPort = 8947
CommandExecutionDaemonRunnerXMLRPCPort = 8948

SHARED = ''

#determine if this is a windows box or not
windows = False
if 'OS' in os.environ:
  windows = "windows" in os.environ['OS'].lower()

if windows:
  SHARED = '//athena/zshare'
else:
  SHARED = '/zshare'


class CommandExecutionDaemonServer(RequestServer):
  """
  Handle xml-rpc requests
  """
  def __init__(self, ip):
    RequestServer.__init__(self, ip, port = CommandExecutionDaemonServerXMLRPCPort)
    self.ip = ip

  def checkConnection(self):
    """
    For sanity checking
    """
    return "Connection to CommandExecutionDaemonServer OK"

  def runRemoteCommand(self, cmdlist, synchronous=False):
    if cmdlist[0] not in legalCommands: return "ERROR: Illegal command " + cmdlist[0]
    print "Executing", " ".join(cmdlist)
    try:
        #### Careful here: is this actually spawning a subprocess,
        #### Or is it waiting to finish before it continues????
        p = subprocess.Popen(cmdlist, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, shell=True)
        if synchronous:
            #p.wait()
            print 'subprocess returned'
        else:
            print 'subprocess started'
        pid = p.pid
    except Exception, e:
        s = "EXCEPTION %s: CommandExecutionDaemon Exception caught running subprocess.Popen().\n%s\n" % (GetTrueIP.machineName(), str(e))
        print s
        return s
    print "pid:", pid
    currentlyRunningCommands[str(pid)] = cmdlist
    stdOutThread = Thread(target=lambda:self.stdOutWriter(p)).start()
    return (pid)
  
  def stdOutWriter(self,process): 
    # for now, write stdout lines to a text file that can be read by clients.
    pid = process.pid
    try:
        print 'forming filename on %s' % GetTrueIP.machineName()
        filename = r"%s/models/pythonSrc/tmp/%s/stdout.txt" % (SHARED, GetTrueIP.machineName())
        print filename
        f = open(filename, 'w')
        f.write('Machine: %s, PID: %d, Created: %s\n' % (GetTrueIP.machineName(), pid, time.asctime()))
        try:
            for line in process.stdout:
                f.write(line)
                f.flush()
        finally:
            f.close()
    except Exception, e:
        s = "EXCEPTION %s: CommandExecutionDaemon Exception caught writing Popen.stdout.\n%s\n" % (GetTrueIP.machineName(), str(e))
        print s
        return s
    return "finished"

  def killRemoteCommand(self, pid):
    if not str(pid) in currentlyRunningCommands:
        return "ERROR: pid [%s] not in running list %s" % (pid, str(currentlyRunningCommands))
    if windows:
        result = os.system('TASKKILL /F /T /PID ' + str(pid))
    else:
        result = os.kill(pid) #### TODO: This may not kill all the child processes
    del currentlyRunningCommands[pid]
    return result

  def getProcessList(self):
    if windows:
    	proc = ["tasklist"]
    else:
    	proc = ["ps"]
    return subprocess.Popen(proc, stdout=subprocess.PIPE).communicate()[0]

  def getSharedFolder(self):
    return SHARED

  def terminate(self):
        """
        For restarting by CommandExecutionDaemon.py
        A hard stop (might be a better way to do this)
        """
        os.abort()

if __name__ == "__main__":
  ipAddress = GetTrueIP.trueIP()
  print "CommandExecutionDaemonServer running"
  CommandExecutionDaemonServer(ipAddress)
