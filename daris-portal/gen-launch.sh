mvn -Duse.google.eclipse.plugin=false gwt:eclipse

# after launch file is generated, you need to edit it, add "-noserver -nostartServer -nosuperDevMode -codeServerPort 9997" to org.eclipse.jdt.launching.PROGRAM_ARGUMENTS attribute, like below:
# <stringAttribute key="org.eclipse.jdt.launching.PROGRAM_ARGUMENTS" value="-war target/daris-portal-1.0.1  -noserver -nostartServer -nosuperDevMode -codeServerPort 9997 -startupUrl DaRIS.html daris.DaRIS"/>