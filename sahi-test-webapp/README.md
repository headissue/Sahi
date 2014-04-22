## Integration Tests

This package is meant to provide a set of sample .html files and test suites to make sure the API works.

Deploy the webapp and run the tests.

As the testrunner should not be used, the integration suite has to be invoked via request

    export SAHI_PROXY="http://localhost:9999"
    export PATH_TO_SUITE=".....toProject/sahi-test-webapp/src/main/sahi/suites/integration.suite"
    curl -v "${SAHI_PROXY}/_s_/dyn/Suite_start?suite=${PATH_TO_SUITE}&base=http://sahi.example.com/_s_/dyn/Driver_initialized&threads=1&sahisid=sahi_b3d244c306259042b90943f0256ac9ebadd2&browserType=firefox&html=${PATH_TO_LOG}"

Repeatedly check the status with:

    curl -v "http://localhost:9999/_s_/dyn/Suite_status?s&sahisid=sahi_b3d244c306259042b90943f0256ac9ebadd2"


