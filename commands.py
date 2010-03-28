# Scala
import sys,os,inspect
	
# ~~~~~~~~~~~~~~~~~~~~~~ New
if play_command == 'new':
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

# ~~~~~~~~~~~~~~~~~~~~~~ Precompile
if play_command == 'precompile':
    # replace last element with the console app
    java_cmd[len(java_cmd)-1]="play.mvc.PreCompiler"
    java_cmd.insert(2, '-Xmx256M -Xms32M')

if play_command == 'scalate:precompile':
    check_application()
    load_modules()
    do_classpath()
    #add precompiled classes to classpath
    cp_args += ":"+os.path.normpath(os.path.join(application_path,'tmp/classes'))
    do_java()
    if os.path.exists(os.path.join(application_path, 'tmp')):
        shutil.rmtree(os.path.join(application_path, 'tmp'))
    java_cmd.insert(2, '-Dprecompile=yes')
    # replace last element with the console app
    java_cmd[len(java_cmd)-1]="play.mvc.PreCompiler"
    java_cmd.insert(2, '-Xmx256M -Xms32M')
    subprocess.call(java_cmd, env=os.environ)
    print
    sys.exit(0)

# ~~~~~~~~~~~~~~~~~~~~~~ Eclipsify
if play_command == 'ec' or play_command == 'eclipsify':
    dotProject = os.path.join(application_path, '.project')
    replaceAll(dotProject, r'org\.eclipse\.jdt\.core\.javabuilder', "ch.epfl.lamp.sdt.core.scalabuilder")
    replaceAll(dotProject, r'<natures>', "<natures>\n\t\t<nature>ch.epfl.lamp.sdt.core.scalanature</nature>")
