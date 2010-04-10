# Scala
import sys,os,inspect

from framework.pym.utils import *

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
        shutil.copyfile(os.path.join(module_dir,'resources/Application.scala'), os.path.join(application_path, 'app/controllers/Application.scala'))
        shutil.copyfile(os.path.join(module_dir,'resources/Model.scala'), os.path.join(application_path, 'app/models/Model.scala'))
        shutil.copyfile(os.path.join(module_dir,'resources/index.ssp'), os.path.join(application_path, 'app/views/Application/index.ssp'))
        shutil.copyfile(os.path.join(module_dir,'resources/default.ssp'), os.path.join(application_path, 'app/views/default.ssp'))
        shutil.copyfile(os.path.join(module_dir,'resources/500.scaml'), os.path.join(application_path, 'app/views/errors/500.scaml'))
        shutil.copyfile(os.path.join(module_dir,'resources/500.css'), os.path.join(application_path, 'public/stylesheets/500.css'))
        f = open(os.path.join(application_path, 'conf/application.conf'),'a')
        f.write('\n\n#scalate config\nscalate=ssp\njvm.memory=-Xmx256M -Xms32M')
        f.close()
        os.remove(os.path.join(application_path, 'app/views/Application/index.html'))
        os.remove(os.path.join(application_path, 'app/views/main.html'))


def before(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")
    if command == 'precompile':
        app.check()
        java_cmd = app.java_cmd(args)
        #add precompiled classes to classpath
        app.cp_args += ":"+os.path.normpath(os.path.join(application_path,'tmp/classes'))
        if os.path.exists(os.path.join(application_path, 'tmp')):
            shutil.rmtree(os.path.join(application_path, 'tmp'))
        # replace last element with the console app
        java_cmd[len(java_cmd)-1]="play.mvc.PreCompiler"
        java_cmd.insert(2, '-Xmx256M -Xms32M')

