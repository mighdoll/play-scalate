# Scala
import sys,os,inspect,subprocess

from play.utils import *

MODULE = 'scalate'

COMMANDS = []

def execute(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")
	
# ~~~~~~~~~~~~~~~~~~~~~~ New

def after(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")
    if command == 'new':
        module_dir = inspect.getfile(inspect.currentframe()).replace("commands.py","")
        shutil.copyfile(os.path.join(module_dir,'resources/Application.scala'), os.path.join(app.path, 'app/controllers/Application.scala'))
        shutil.copyfile(os.path.join(module_dir,'resources/Model.scala'), os.path.join(app.path, 'app/models/Model.scala'))
        shutil.copyfile(os.path.join(module_dir,'resources/index.ssp'), os.path.join(app.path, 'app/views/Application/index.ssp'))
        shutil.copyfile(os.path.join(module_dir,'resources/default.ssp'), os.path.join(app.path, 'app/views/default.ssp'))
        shutil.copyfile(os.path.join(module_dir,'resources/500.scaml'), os.path.join(app.path, 'app/views/errors/500.scaml'))
        shutil.copyfile(os.path.join(module_dir,'resources/500.css'), os.path.join(app.path, 'public/stylesheets/500.css'))
        f = open(os.path.join(app.path, 'conf/application.conf'),'a')
        f.write('\n\n#scalate config\nscalate=ssp\njvm.memory=-Xmx256M -Xms32M')
        f.close()
        os.remove(os.path.join(app.path, 'app/views/Application/index.html'))
        os.remove(os.path.join(app.path, 'app/views/main.html'))


def before(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")
    if command == 'precompile':
        app.check()
        java_cmd = app.java_cmd(args)
        #add precompiled classes to classpath
        if os.path.exists(os.path.join(app.path, 'tmp')):
            shutil.rmtree(os.path.join(app.path, 'tmp'))
        if os.path.exists(os.path.join(app.path, 'precompiled')):
            shutil.rmtree(os.path.join(app.path, 'precompiled'))
        # replace last element with the console app
        java_cmd[3]=java_cmd[3]+":"+os.path.normpath(os.path.join(app.path,'tmp/classes'))
        java_cmd[len(java_cmd)-1]="play.mvc.PreCompiler"
        java_cmd.insert(2, '-Xmx256M')
        try:
            subprocess.call(java_cmd, env=os.environ)
        except OSError:
            print "Could not execute the java executable, please make sure the JAVA_HOME environment variable is set properly (the java executable should reside at JAVA_HOME/bin/java). "
            sys.exit(-1)
        #copy classed to precompiled
        shutil.copytree(os.path.join(app.path, 'tmp/classes'),os.path.join(app.path, 'precompiled/java'))
        sys.exit(0)


