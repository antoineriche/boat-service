#!/usr/bin/env groovy
@Library('pfPipeline@tags/4') _

// Notifications
def NOTIFICATION_SEND_TO = 'antoine.riche@bnpparibas-pf.com'

pipeline {

    agent none

    options {
        gitLabConnection('gitlab-dogen.group.echonet')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '5', artifactNumToKeepStr: '5'))
        timeout(time: 1, unit: 'HOURS')
        preserveStashes()
        ansiColor('xterm')
    }

    triggers {
        cron(env.BRANCH_NAME == 'develop' ? 'H H(0-7) * * *' : '')
        gitlab(triggerOnPush: true, triggerOnMergeRequest: true, branchFilterType: 'All')
    }

    parameters {
        booleanParam(name: "SKIP_BUILD", description: "Skip Build Step (require if docker release is already done)", defaultValue: false)
    }

    stages {

        stage('prepare') {
            agent any
            stages {
                stage('display-name'){
                    steps {
                        script {
                            ARTIFACT_VERSION = pfUtilities.getPomVersion(pom: 'pom.xml', release: params.RELEASE)
                            currentBuild.displayName = "#${BUILD_NUMBER} - ${ARTIFACT_VERSION}"
                        }
                    }
                }
                stage('maven-release-skip') {
                    when {
                        changelog '^\\[maven-release-plugin\\].*'
                    }
                    steps {
                        script {
                            currentBuild.result = 'NOT_BUILT'
                        }
                        error('Skipping release build')
                    }
                }
            }
        }

        stage('(CI) build') {
            when {
                not {
                    anyOf {
                        tag "release/*"
                        expression { params.SKIP_BUILD }
                    }
                }
                beforeAgent true
            }
            agent {
                label 'docker && java'
            }
            environment {
                ARTIFACT_ID = readMavenPom(file: 'pom.xml').getArtifactId()
                ARTIFACT_VERSION = pfUtilities.getPomVersion(pom: 'pom.xml', release: params.RELEASE)
            }
            stages {
                stage("scan") {
                    steps {
                        // security
                        pfMvnBuild(goals: '-U com.sonatype.clm:clm-maven-plugin:index')
                        pfIQServerAnalysis (
                            iqApplication: ARTIFACT_ID,
                            iqStage: 'build',
                            iqScanPatterns: [[scanPattern: '**/sonatype-clm/module.xml']]
                        )
                    }
                }
                stage('gate-controls') {
                    when {
                        expression {params.RELEASE}
                    }
                    steps {
                        // only done on release
                        // need evolution to have unstable state when in development
                        // problem with sonar execution in maven
                        pfGate(skipQuality: true)
                    }
                }
                stage('build') {
                    steps {
                        // maven (launch unit-tests & sonarqube analysis)
                        script {
                            if (params.RELEASE) {
                                pfMvnRelease(extraGoalsAndOptions: '-U')
                            } else {
                                pfMvnBuild(options: '-U')
                            }
                        }
                    }
                }
            }
        }

    }

}
