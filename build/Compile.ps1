Set-PSDebug -Trace 1

$mainclass_path = '../src/pack/Main.java'

$build_outp = 'bin'
$lwjgl_dir = '../lwjgl-2.8.5'
$lwjgl_dll_dir = $lwjgl_dir + '/native/windows'
$lwjgl_jar = $lwjgl_dir + '/jar/lwjgl.jar'

$jar_outp = 'jar'
$jar_name = 'AStar.jar'
$jar_manifest = 'MANIFEST.MF'

If(!(Test-Path $build_outp)) { New-Item -ItemType Directory -Path $build_outp }
If(!(Test-Path $jar_outp)) { New-Item -ItemType Directory -Path $jar_outp }

echo 'Compiling...'
javac -cp ($lwjgl_jar + ";../src/") $mainclass_path -d $build_outp
echo 'Compiling done'

Copy-Item $lwjgl_jar $jar_outp
Copy-Item ($lwjgl_dll_dir + '/lwjgl.dll') $jar_outp
Copy-Item ($lwjgl_dll_dir + '/lwjgl64.dll') $jar_outp

echo 'Creating executable jar...'
jar -cfme ($jar_outp + '\' + $jar_name) $jar_manifest pack.Main -C $build_outp pack
echo 'Jar creation done'

pause
