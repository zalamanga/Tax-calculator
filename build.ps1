param(
    [ValidateSet('Build', 'Test', 'Run')]
    [string]$Task = 'Build'
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$projectRoot = $PSScriptRoot

function Invoke-JavaTool([string]$Name, [string[]]$Arguments) {
    $executable = $null
    if ($env:JAVA_HOME) {
        $candidate = Join-Path $env:JAVA_HOME "bin/$Name"
        if (Test-Path -LiteralPath "$candidate.exe") { $executable = "$candidate.exe" }
        elseif (Test-Path -LiteralPath $candidate) { $executable = $candidate }
    }
    if (-not $executable) {
        $command = Get-Command $Name -ErrorAction SilentlyContinue
        if (-not $command) { throw "JDK 11+ diperlukan; $Name tidak ditemukan. Atur JAVA_HOME atau PATH." }
        $executable = $command.Source
    }
    & $executable @Arguments
    if ($LASTEXITCODE -ne 0) { throw "$Name gagal (exit $LASTEXITCODE)." }
}

function Reset-BuildDirectory([string]$Path) {
    $resolved = [IO.Path]::GetFullPath($Path)
    $allowed = [IO.Path]::GetFullPath((Join-Path $projectRoot 'build')) + [IO.Path]::DirectorySeparatorChar
    if (-not $resolved.StartsWith($allowed, [StringComparison]::OrdinalIgnoreCase)) {
        throw 'Direktori output berada di luar build/.'
    }
    if (Test-Path -LiteralPath $resolved) { Remove-Item -LiteralPath $resolved -Recurse -Force }
    New-Item -ItemType Directory -Path $resolved -Force | Out-Null
}

Push-Location -LiteralPath $projectRoot
try {
    $classes = Join-Path $projectRoot 'build/classes'
    Reset-BuildDirectory $classes
    $sources = @(Get-ChildItem -LiteralPath (Join-Path $projectRoot 'src') -Filter '*.java' -Recurse -File)
    if (-not $sources.Count) { throw 'Tidak ada source Java di src/.' }
    Invoke-JavaTool 'javac' (@('--release', '8', '-encoding', 'UTF-8', '-d', $classes) + @($sources.FullName))

    $distribution = Join-Path $projectRoot 'dist'
    New-Item -ItemType Directory -Path $distribution -Force | Out-Null
    $jar = Join-Path $distribution 'tax-calculator.jar'
    Invoke-JavaTool 'jar' @('--create', '--file', $jar, '--main-class', 'taxcalculator.TaxCalculator', '-C', $classes, '.')
    Write-Host 'Build berhasil: dist/tax-calculator.jar (target Java 8).'

    if ($Task -eq 'Test') {
        $testClasses = Join-Path $projectRoot 'build/test/classes'
        Reset-BuildDirectory $testClasses
        $testSources = @(Get-ChildItem -LiteralPath (Join-Path $projectRoot 'test') -Filter '*.java' -Recurse -File)
        if (-not $testSources.Count) { throw 'Tidak ada test Java di test/.' }
        Invoke-JavaTool 'javac' (@('--release', '8', '-encoding', 'UTF-8', '-cp', $classes, '-d', $testClasses) + @($testSources.FullName))
        $classpath = $classes + [IO.Path]::PathSeparator + $testClasses
        $tests = @($testSources | Where-Object { $_.BaseName.EndsWith('Test') })
        if (-not $tests.Count) { throw 'Tidak ditemukan kelas *Test.' }
        foreach ($test in $tests) {
            $source = [IO.File]::ReadAllText($test.FullName)
            $package = [regex]::Match($source, '(?m)^\s*package\s+([\w.]+)\s*;')
            $class = if ($package.Success) { $package.Groups[1].Value + '.' + $test.BaseName } else { $test.BaseName }
            Invoke-JavaTool 'java' @('-ea', '-cp', $classpath, $class)
        }
        Write-Host "Test berhasil: $($tests.Count) test class."
    } elseif ($Task -eq 'Run') {
        Invoke-JavaTool 'java' @('-jar', $jar)
    }
} finally {
    Pop-Location
}
