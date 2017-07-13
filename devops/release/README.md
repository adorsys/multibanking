## To release:

### Preparation
* Use SSH connection to github:
```bash
$ git remote show origin
$ git remote show origin
* remote origin
  Fetch URL: git@github.com:adorsys/multibanking.git
  Push  URL: git@github.com:adorsys/multibanking.git
...
```
* Get your OSS Nexus Account [here](https://issues.sonatype.org/secure/Signup!default.jspa)
* Contact Your repository Administrator to get release rights
* Setup your Maven repository in your settings.xml:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.1.0 http://maven.apache.org/xsd/settings-1.1.0.xsd">
	<servers>
		<server>
			<id>sonatype</id>
			<username>my-sonatype-username</username>
			<password>{my-sonatype-maven-encrypted-password}</password>
		</server>
	</servers>
</settings>

```
### Release
* Checkout and update develop branch: ``$ git checkout develop && git pull``
* Re-build project: ``$ mvn clean install``
* Check for javadoc errors: ``$ mvn javadoc:javadoc``
* Cleanup from possible previous releases ``$ mvn release:clean``
* Set release versions and tags with ``$ mvn release:prepare``
* Perform upload to Nexus with ``$ mvn release:perform``
* Open OSS Nexus and follow [instruction](http://central.sonatype.org/pages/releasing-the-deployment.html):
    * login
    * find your staging repository
    * close your staging repository
    * release and drop your staging repository
* Open Github und make PR from develop to master, to fix the code state on release (tag is already created with release:prepare)
