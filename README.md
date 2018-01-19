# Slow Query Simulator #
## ES Version ##
6.0.1

Adjust `dependencies { classpath "org.elasticsearch.gradle:build-tools:6.0.1" }` in `build.gradle` to change.

## Build ##
Assuming your `JAVA_HOME` is at `/usr/lib/jvm/java-8-jdk/`
```
export JAVA_HOME=/usr/lib/jvm/java-8-jdk/
gradle assemble
```

## Import Plugin Into ES Using ECE
Assuming the zip is hosted at `http://192.168.44.10:7777/slow-query-simulator.zip` (eg `darkhttpd ~/plugins --port 7777 --addr 192.168.44.10`), add the following in the plan:
```json
  "user_plugins": [
    {
      "name": "custom-plugin",
      "url": "http://192.168.44.10:7777/slow-query-simulator.zip",
      "elasticsearch_version": "6.0.1"
    }
  ]
```

## Create An Index ##
`curl --user "elastic:${PASSWORD}" -H 'Content-Type: application/json' -XPUT "https://${CLUSTER}/${INDEX}/test/1" -d '{"is-slow": true }'`

## Execute A Slow Query ##
`sleepSec` is the number of seconds to sleep in the query handler. `slow` identifies the plugin.
```bash
curl --user "elastic:${PASSWORD}" -H 'Content-Type: application/json' "https://${CLUSTER}:9243/${INDEX}/_search" -d '{
  "query": {
    "slow": {
      "sleepSec": 5
    }
  }
}'
```
```json
{"took":5002,"timed_out":false,"_shards":{"total":5,"successful":5,"skipped":0,"failed":0},"hits":{"total":0,"max_score":null,"hits":[]}}
```
