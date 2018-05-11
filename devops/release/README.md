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

* Change to latest state of `develop`-branch:

```bash
$ git checkout develop && git pull
```

* Run the release-script:

```bash
$ ./release-script/release ${RELEASE_VERSION} ${NEXT_VERSION}
```

For example, if you want to release version `3.2.1` and want the next SNAPTSHOT version in your pom set to `3.3.0-SNAPSHOT`, you have to run following command:

```bash
$ ./release-script/release 3.2.1 3.3.0
```

Consider [release-scripts documentation](https://github.com/borisskert/release-scripts).
The release-script will NOT push anything into remote automatically but it will perform some commits, merges and will create a git tag.

* The release-script will inform you about how to push the changes into the remote. The command looks like the following:

```bash
$ git push --atomic ${REMOTE_REPO} ${MASTER_BRANCH} ${DEVELOP_BRANCH} --follow-tags
```

* Travis will perform the artifact build and upload to oss.sonatype.org automatically. Have a look at the progress here: [multibanking on travis](https://travis-ci.org/adorsys/multibanking)

* The java-artifacts should be accessible in maven central after some minutes (maybe hours, if oss.sonatype.org has a bad day).
 