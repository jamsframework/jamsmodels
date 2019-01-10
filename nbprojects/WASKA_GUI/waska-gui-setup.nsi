;NSIS Modern User Interface
;JAMS Install Script with JRE checking
;Written by Sven Kralisch

;--------------------------------
;Include some stuff

  !include "MUI2.nsh"
  !include "x64.nsh"                    
  
  !define JRE_VERSION "1.7"
  !define JRE_URL_32 "http://javadl.oracle.com/webapps/download/AutoDL?BundleId=207773"
  !define JRE_URL_64 "http://javadl.oracle.com/webapps/download/AutoDL?BundleId=207775"
  !define JRE_URL "http://javadl.oracle.com/webapps/download/AutoDL?BundleId=207772"
;  !include "software\JREDyna_Inetc.nsh"  
  !include "scripts\JREDyna.nsh"
  !include "scripts\fileassoc.nsh"

;--------------------------------
;General

  ;Name and file
  Name "WASKA GUI"
  OutFile "bin\waska-gui-setup.exe"
  
  ;Default installation folder
  InstallDir "C:\Program Files (x86)\waska"

  ;Get installation folder from registry if available
  InstallDirRegKey HKCU "Software\waska" ""
  
  !define NAME "WASKA GUI"

;--------------------------------
;Variables

  Var MUI_TEMP
  Var STARTMENU_FOLDER
                                                                                    
;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING
                                                  
;--------------------------------
;Pages

  !insertmacro MUI_PAGE_WELCOME
  ;!insertmacro MUI_PAGE_LICENSE "..\jams\JAMSmain\src\resources\text\license.txt"
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro CUSTOM_PAGE_JREINFO      
  
  ;Start Menu Folder Page Configuration
  !define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKCU" 
  !define MUI_STARTMENUPAGE_REGISTRY_KEY "Software\Modern UI Test" 
  !define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "Start Menu Folder"
  
  !insertmacro MUI_PAGE_STARTMENU Application $STARTMENU_FOLDER
    
  !insertmacro MUI_PAGE_INSTFILES
  !insertmacro MUI_PAGE_FINISH

  !insertmacro MUI_UNPAGE_WELCOME
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  !insertmacro MUI_UNPAGE_FINISH
  

;--------------------------------
;Languages

  !insertmacro MUI_LANGUAGE "English"


;--------------------------------
;Installer Sections

Section "WASKA" SecWASKA
	SectionIn RO

  SetOutPath "$INSTDIR"
  File /r /x ".svn" "dist\*"
  ;File /r /x "JAMSWikiDoc" /x ".svn" "..\jams\bin"
  ;File /r /x ".svn" "..\jams\docbook"
  ;File /r /x "JAMSWikiDoc" /x ".svn" /x "nbprojects" /x "default.jap" /x "Tools" /x "docbook" /x "JAMSworldwind" "..\jams\jams-bin\*" 
  ;File /r /x ".svn" "..\jamsmodels\nbprojects\components" 

;  SetOutPath "$INSTDIR\lib\lib"
;  ${If} ${RunningX64}
;    File /r /x "JAMSWikiDoc" /x ".svn" "..\jams\bin\win64\*"
;  ${Else}
;    File /r /x "JAMSWikiDoc" /x ".svn" "..\jams\bin\win32\*"
;  ${EndIf}
  
  SetOutPath "$INSTDIR"
  
  AccessControl::GrantOnFile "$INSTDIR" "(BU)" "FullAccess"
 
  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    ;Create shortcuts
    CreateDirectory "$SMPROGRAMS\$STARTMENU_FOLDER"
    CreateShortCut "$DESKTOP\WASKA GUI.lnk" "$INSTDIR\WASKA_GUI.exe"
    ;CreateShortCut "$DESKTOP\JAMS Remote Launcher.lnk" "$INSTDIR\jamsremote.exe"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\WASKA GUI.lnk" "$INSTDIR\WASKA_GUI.exe"
    ;CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\JAMS Builder.lnk" "$INSTDIR\juice.exe"
;    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\JAMS Remote Launcher.lnk" "$INSTDIR\jamsremote.exe"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\WASKA Website.lnk" "http://waska.uni-jena.de"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Test datasets.lnk" "$INSTDIR\data"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Uninstall.lnk" "$INSTDIR\Uninstall.exe"    
  !insertmacro MUI_STARTMENU_WRITE_END
  
;  !insertmacro APP_ASSOCIATE "jam" "jams.modelfile" "JAMS model file" "$INSTDIR\jams.exe,0" "Load with JAMS" "$INSTDIR\jams.exe $\"%1$\""

   ;call DownloadAndInstallJREIfNecessary   

SectionEnd

;Section "Test datasets" SecTestdata

;  SetOutPath "$INSTDIR"

  ;ADD YOUR OWN FILES HERE...
  ;File /r /x ".svn" "jamsdata\*"

;  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    ;Create shortcuts
;    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Test datasets.lnk" "$INSTDIR\data"
;  !insertmacro MUI_STARTMENU_WRITE_END

;SectionEnd

Section "Java" SecJRE

  call DownloadAndInstallJREIfNecessary

SectionEnd

;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_WASKA ${LANG_ENGLISH} "Files required to run WASKA GUI"
  ;LangString DESC_DATA ${LANG_ENGLISH} "Test datasets for the J2000 and Thorntwaite models"
  LangString DESC_JRE ${LANG_ENGLISH} "Download and install required Java version if necessary"

  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SecWASKA} $(DESC_WASKA)
  ;!insertmacro MUI_DESCRIPTION_TEXT ${SecTestdata} $(DESC_DATA)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecJRE} $(DESC_JRE)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  ;ADD YOUR OWN FILES HERE...

  RMDir /r "$INSTDIR"
  
  !insertmacro MUI_STARTMENU_GETFOLDER Application $MUI_TEMP
  Delete "$DESKTOP\WASKA GUI.lnk"
  ;Delete "$DESKTOP\JAMS Builder.lnk"
  ;Delete "$DESKTOP\JAMS Remote Launcher.lnk"
  Delete "$SMPROGRAMS\$MUI_TEMP\WASKA GUI.lnk"
  ;Delete "$SMPROGRAMS\$MUI_TEMP\JAMS Builder.lnk"
;  Delete "$SMPROGRAMS\$MUI_TEMP\JAMS Remote Launcher.lnk"
  Delete "$SMPROGRAMS\$MUI_TEMP\Test datasets.lnk"
  Delete "$SMPROGRAMS\$MUI_TEMP\WASKA Website.lnk"
  Delete "$SMPROGRAMS\$MUI_TEMP\Uninstall.lnk"  
  
  ;Delete empty start menu parent diretories
  StrCpy $MUI_TEMP "$SMPROGRAMS\$MUI_TEMP"
 
  startMenuDeleteLoop:
	ClearErrors
    RMDir $MUI_TEMP
    GetFullPathName $MUI_TEMP "$MUI_TEMP\.."
    
    IfErrors startMenuDeleteLoopDone
  
    StrCmp $MUI_TEMP $SMPROGRAMS startMenuDeleteLoopDone startMenuDeleteLoop
  startMenuDeleteLoopDone:
  

SectionEnd

