Set-Location $PSScriptRoot

function Install-One($Artifact) {
    $jar = Get-ChildItem -Path "jams-libs" -Filter "$Artifact-*.jar" -File -ErrorAction SilentlyContinue |
        Sort-Object Name | Select-Object -First 1
    if (-not $jar) {
        Write-Error "No $Artifact-*.jar found in jams-libs/"
        exit 1
    }
    Write-Output "Installing $($jar.Name) as org.jams:${Artifact}:local"
    & .\mvnw.cmd -q org.apache.maven.plugins:maven-install-plugin:3.1.2:install-file `
        "-Dfile=$($jar.FullName)" "-DgroupId=org.jams" "-DartifactId=$Artifact" `
        "-Dversion=local" "-Dpackaging=jar" "-DgeneratePom=true"
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

Install-One "jams-api"
Install-One "jams-main"
