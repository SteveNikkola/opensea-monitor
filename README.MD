![opensea_monitor_logo](./docs/os-monitor-logo.png)

---
opensea-monitor is an application stack to monitor the OpenSea marketplace for newly listed and sold NFTs

`PLEASE NOTE:` this application/stack should not be run as is. There are a couple reasons for this:
* the code regarding the core logic for calling OpenSea APIs would need to be updated to use an API Key, and will be done so as soon as I am provided an API Key from OpenSea. 
* this application should be updated to use the OpenSea [Stream API](https://docs.opensea.io/reference/stream-api-overview) to listen to events, as that is a much better option. Not only in terms of not having to make API calls on the application side, but it also lowers load on OpenSea. Being a responsible consumer should always be a major consideration when using another system's services, and moving to this integration will allow me to adhere to that principal. 
  * Additionally, once converted to use the `Stream API`, this whole stack can be simplified to remove the usage of `Airflow` as a scheduler system. I have not yet developed against the `Stream API`, as I am unable to justify the development time until I know I will be provided an API key, but it will be the first thing I do.

In summary, this repo is demonstration of me being serious about developing applications against OpenSea `events`, with an API key, and will be largely refactored once I am given the chance to do so.

---
## Overview

### Requirements
All that is required to run opensea-monitor is `Docker Compose`. Installation info can be found in Docker's documentation: [Install Docker Compose](https://docs.docker.com/compose/install/).

---

### Technology & Architecture

**Technology**

opensea-monitor Uses a number of different technologies to run the various services. Below I have highlighted a subset of these technologies that can be further explored, if desired:

*Orchestration/Scheduling*
* [Docker](https://www.docker.com/)
* [Apache Airflow](https://airflow.apache.org/)

*Development*
* [Kotlin](https://kotlinlang.org/)
* [Spring Boot](https://spring.io/projects/spring-boot)
* [Python](https://www.python.org/)

*Database/DB Management*
* [PostgreSQL](https://www.postgresql.org/)
* [MongoDB](https://www.mongodb.com/home)
* [Redis](https://redis.io/)


**Architecture**

![opensea-monitor-architecture](./docs/opensea-monitor-architecture.png)


----

## Quick-Start

### Environment Variables

There are a number of environment variables preconfigured that can be seen in the [.env](.env) file. You do not need to change anything here to start the app, however you may want to consider changing pieces such as database username/passwords, and the airflow `admin` user info, along with other keys such as the `AIRFLOW__CORE__FERNET_KEY` and `AIRFLOW__WEBSERVER__SECRET_KEY`.

### Start-Up

*note: depending on your system/setup, you may have to run commands seen in the below section as an admin user, or using `sudo`*

1. After cloning the repo and (optional) changing any env variable secrets in the [.env](./.env) file, review the [Airflow](#airflow) section to properly configure the [opensea_monitor_config.json](./opensea-monitor-airflow/config/opensea_monitor_config.json) configuration file.


2. The services can now be started with the following `docker compose` command:
    ```shell
    docker compose up -d
    ```

3. The services/containers will begin to start. The way the [docker-compose.yml](./docker-compose.yml) is setup, many services have `depends_on` criteria that ensures other services are up & healthy before they start. Therefore, you will see a list of containers starting up and waiting for others to be healthy. You may at this point see that `airflow-init` will show as `Exited` here. This is normal - `airflow-init` performs db and other initial setup for the `airflow-webserver` and `airflow-scheduler` services to be able to run.


4. At any point, you can open a separate terminal session to see an alternate view for tracking the progress of the services building with the following command:
    ```shell
    docker compose ps
    ```
   And you should see something like this (the number of services running will depend on what part of the startup process you are at):
   ```shell
    NAME                                      COMMAND                  SERVICE                 STATUS               PORTS
    airflow-init                              "/bin/bash -c 'mkdir…"   airflow-init            created
    airflow-scheduler                         "/usr/bin/dumb-init …"   airflow-scheduler       created
    airflow-webserver                         "/usr/bin/dumb-init …"   airflow-webserver       created
    opensea-monitor-airflow-celery-worker-1   "/usr/bin/dumb-init …"   airflow-celery-worker   created
    opensea-monitor-airflow-celery-worker-2   "/usr/bin/dumb-init …"   airflow-celery-worker   created
    opensea-monitor-api                       "/opensea-monitor-ap…"   opensea-monitor-api     created
    opensea-monitor-mongo-1                   "docker-entrypoint.s…"   mongo                   running (starting)   0.0.0.0:27017->27017/tcp
    opensea-monitor-mongo-express-1           "tini -- /docker-ent…"   mongo-express           created
    postgres                                  "docker-entrypoint.s…"   postgres                running (starting)   0.0.0.0:5432->5432/tcp
    redis                                     "docker-entrypoint.s…"   redis                   created
    ```
   Note: `airflow-init` may report State as `Exit 0`, depending on what point in the startup you are at. This is ok. 


5. If any services are not reporting as `Up` or in one of the healthy states, or in general you want to watch for any error logs, run the following from a separate terminal session to see all logs from all services:
     ```shell
     docker compose logs -f
     ```
    If you want to view the logs for one or multiple services, but not all, simply run the same command but specify which service(s) to show logs from:
     ```shell
     docker compose logs -f postgres opensea-monitor-api
     ```

6. The `opensea-monitor-api` will probably take the longest to startup for the first time (20 seconds to perhaps 5-10 minutes, depending on your machine). You may want to specifically view logs for that service and wait to see the application fully started and ready. Once it has completed, along with all other services, you will regain access to the terminal and should see something like the following.
   ```shell
    [+] Running 14/14
    ⠿ Network opensea-monitor_default                    Created                                                                                                                                             0.0ss
    ⠿ Volume "opensea-monitor_airflow-logs"              Created                                                                                                                                             0.0ss
    ⠿ Volume "opensea-monitor_postgres-data"             Created                                                                                                                                             0.0ss
    ⠿ Volume "opensea-monitor_mongo-data"                Created                                                                                                                                             0.0ss
    ⠿ Container opensea-monitor-mongo-1                  Started                                                                                                                                             0.5ss
    ⠿ Container postgres                                 Started                                                                                                                                             0.5ss
    ⠿ Container airflow-init                             Started                                                                                                                                            11.3ss
    ⠿ Container opensea-monitor-api                      Started                                                                                                                                            11.3ss
    ⠿ Container opensea-monitor-mongo-express-1          Started                                                                                                                                            11.2ss
    ⠿ Container airflow-webserver                        Started                                                                                                                                            26.7ss
    ⠿ Container redis                                    Started                                                                                                                                            26.7ss
    ⠿ Container opensea-monitor-airflow-celery-worker-2  Started                                                                                                                                            32.5ss
    ⠿ Container airflow-scheduler                        Started                                                                                                                                            32.5ss
    ⠿ Container opensea-monitor-airflow-celery-worker-1  Started                                                                                                                                            32.4ss
   ```

   Additionally, if you run `docker compose ps` again, you will see all services up and running/healthy (except `airflow-init`, which as noted earlier should've successfully exited):

   ```shell
    NAME                                      COMMAND                  SERVICE                 STATUS              PORTS
    airflow-init                              "/bin/bash -c 'mkdir…"   airflow-init            exited (0)
    airflow-scheduler                         "/usr/bin/dumb-init …"   airflow-scheduler       running (healthy)   8080/tcp
    airflow-webserver                         "/usr/bin/dumb-init …"   airflow-webserver       running             0.0.0.0:4444->8080/tcp
    opensea-monitor-airflow-celery-worker-1   "/usr/bin/dumb-init …"   airflow-celery-worker   running (healthy)   8080/tcp
    opensea-monitor-airflow-celery-worker-2   "/usr/bin/dumb-init …"   airflow-celery-worker   running (healthy)   8080/tcp
    opensea-monitor-api                       "/opensea-monitor-ap…"   opensea-monitor-api     running             0.0.0.0:4000->8080/tcp
    opensea-monitor-mongo-1                   "docker-entrypoint.s…"   mongo                   running (healthy)   0.0.0.0:27017->27017/tcp
    opensea-monitor-mongo-express-1           "tini -- /docker-ent…"   mongo-express           running             0.0.0.0:8081->8081/tcp
    postgres                                  "docker-entrypoint.s…"   postgres                running (healthy)   0.0.0.0:5432->5432/tcp
    redis                                     "docker-entrypoint.s…"   redis                   running (healthy)   6379/tcp
   ```

7. If you want to bring down all services at any point, run the following command:
   ```shell
   docker compose down
   ```
   **Read Carefully**
    1. Do the following only if you need to fully reset the state of `opensea-monitor` for any reason. After bringing the containers down run the following command to prune the docker volumes. This will delete any volumes that no longer have an associated container. This means any of the data stored for postgres/mongo will be deleted.
       ```shell
       # READ THE ABOVE NOTE BEFORE RUNNING THIS
       docker volume prune
       ```

## Service Details

### Airflow

Airflow is a platform to programmatically author, schedule and monitor workflows. Official documentation can be found [here](https://airflow.apache.org/docs/apache-airflow/2.3.3/index.html). `opensea-monitor` uses Airflow to configure and schedule requests to OpenSea to request new events. Each collection monitored is an individual Airflow [DAG](https://airflow.apache.org/docs/apache-airflow/2.3.3/concepts/dags.html).

The below screenshot shows an example of the Airflow Admin console. With this tool, we can add/remove which collections to monitor, update how frequently they are monitored, and get a general idea of system health.

![airflow-overview](./docs/airflow-overview.png)

**Why Use Airflow?**

`Airflow` decouples and externalizes app scheduling from the [opensea-monitor-api](#opensea-monitor-api), the service/app that contains the core logic and functions for retrieving events. With `Airflow` managing the tasks for checking for new events, we can add/remove/update monitor configurations without ever having to make changes to the `opensea-monitor-api`.

**Airflow Environment Variables**

**Note**: in the table below, variables prefixed with `AIRFLOW__` (double underscore) are configs natively defined by `Airflow`. See the [Configuration Reference](https://airflow.apache.org/docs/apache-airflow/stable/configurations-ref.html) for more information.
Variables prefixed with a single underscore, such as `AIRFLOW_` are ones I have created for some custom `Airflow` configuration. You may add more native airflow environment variables from the `Configuration Reference`, but in order for them to be used, you will need to add them to the [.env](./.env) file, **and** pass them to the relevant docker service container defined in the [docker-compose.yml](./docker-compose.yml) (likely the `airflow-webserver` and/or `airflow-scheduler` services), by adding the property to the `environment` section.

`**` = Recommended to not change the default value

|  Env Variable | Default | Examples | Details | Required | 
|---|---|---|---|---|
| AIRFLOW__DATABASE__SQL_ALCHEMY_CONN | **`postgresql+psycopg2://${POSTGRES_USER}:${POSTGRES_PASSWORD}@postgres/${POSTGRES_DB}` | | Connection string for airflow backend database | Yes |
| AIRFLOW__CORE__EXECUTOR | CeleryExecutor` | `CeleryExecutor`, `LocalExecutor` | Executor mode for workflow. `opensea-monitor` uses the Celery Executor to allow for distributed workers. | Yes. Out of the box, this is setup to use the `CeleryExecutor` - see the section on the `airflow-celery-worker` for more details and an alternative option for running as `LocalExecutor` (for a smaller container footprint), if desired. |
| AIRFLOW__CORE__FERNET_KEY | `Tng6QfTxioF4oyzJpImeHdXkajFt-lp38rl-_eFIQu8=` | | Airflow uses Fernet to encrypt passwords in the connection configuration and the variable configuration. Setting this key allows for this security enhancement. See more here in Airflow's [documentation](https://airflow.apache.org/docs/apache-airflow/2.3.3/security/secrets/fernet.html), including how you can generate a key with a simple Python script.| Not required, however any sensitive data such as discord web tokens will be stored unencrypted in the airflow database if this value is not set. |
| AIRFLOW__CORE__LOAD_EXAMPLES | **`False` |  | If set to `True`, example DAGs will be loaded in Airflow.  | No |
| AIRFLOW__WEBSERVER__SECRET_KEY= | `youshouldreplacethis` |  | The webserver key is used to authorize requests to Celery workers when logs are retrieved.   | Yes, if you want to view worker logs when using the `CeleryExecutor` option |
| AIRFLOW__CELERY__RESULT_BACKEND | **`db+postgresql://${POSTGRES_USER}:${POSTGRES_PASSWORD}@postgres/${POSTGRES_DB}` |  | Connection string for the Celery workers and executor results backend.  | Yes |
| AIRFLOW__CELERY__BROKER_URL | **`redis://:@redis:6379/0` |  | Connection string for Celery workers and executor to manage tasks queued for work.  | Yes |
| AIRFLOW_CELERY_INSTANCES | 2 |  | The number of Celery Workers to start.  | Yes |
| AIRFLOW_HOME | **`/opt/airflow` |  | Home directory for Airflow | Yes |
| AIRFLOW_RECREATE_ADMIN_USER | `True` | | This will create an admin user to log into the airflow GUI, using the values set in the next 5 properties (those starting with `ADMIN_`). It is recommended to set the `ADMIN_` values, and keep this as `True` the first time you load `opensea-monitor` and Airflow. Subsequent times you start the application, you can set this to `False`, as you do not need to recreate the user after successfully setting it up the first time. | Yes, on initial run - otherwise you will not be able to use the Airflow GUI. |
| AIRFLOW_ADMIN_USERNAME | `admin` | | Username for admin user created when `AIRFLOW_RECREATE_ADMIN_USER` is set to `True` | Not required to be set after initially creating the admin user |
| AIRFLOW_ADMIN_PASSWORD | `admin` | | | Not required to be set after initially creating the admin user |
| AIRFLOW_ADMIN_EMAIL | `admin` | | | Not required to be set after initially creating the admin user |
| AIRFLOW_ADMIN_FIRSTNAME | `admin` | | | Not required to be set after initially creating the admin user |
| AIRFLOW_ADMIN_LASTNAME | `admin` | | | Not required to be set after initially creating the admin user |
| AIRFLOW_UPDATE_CONFIG_VARIABLE | `True` |  | Whether to reload the DAG configs found in the [opensea_monitor_config.json](./opensea-monitor-airflow/config/opensea_monitor_config.json). Note: this will overwrite any changes made from the GUI for this variable, and stored in the database. | Not required after initial setup |
| AIRFLOW_CONFIG_PATH | **`config/opensea_monitor_config.json` |  | Path for the config file for loading `DAGs`/sites to monitor | Not required if `AIRFLOW_UPDATE_CONFIG_VARIABLE` is not `True`, but see subsequent notes on this config. |

**Airflow Services**

`opensea-monitor` contains a few different airflow related services, as defined in the [docker-compose.yml](./docker-compose.yml):

*airflow-init*

The `airflow-init` service runs before any other airflow services - the job of the `airflow-init` service is to setup the database, admin user, and initial `DAG` configurations. This allows the subsequent `Airflow` services to perform their functions.

*airflow-webserver*

The `airflow-webserver` service creates the GUI at [http://localhost:4444/](http://localhost:4444/) that can be used to monitor `DAG` results/performance, and update configurations.

*airflow-scheduler*

The `airflow-scheduler` service creates the container for the `scheduler` to manage `DAG` runs. More information can be found in `Airflow` documentation: [https://airflow.apache.org/docs/apache-airflow/stable/concepts/scheduler.html](https://airflow.apache.org/docs/apache-airflow/stable/concepts/scheduler.html)

*airflow-celery-worker*

The `airflow-celery-worker` service executes DAGs that are scheduled to be run. This allows for easy scaling of workers if you have a lot of tasks. More info on the `Celery Executor` can be seen in Airflow's official documentation: [https://airflow.apache.org/docs/apache-airflow/stable/executor/celery.html](https://airflow.apache.org/docs/apache-airflow/stable/executor/celery.html). This is not really necessary for this implementation with `docker compose` - it would be more useful if you were running a formal container orchestration system such as `Kubernetes`, and had workers scaled across different servers/locations, but I have decided to implement this executor as I like separations of concerns. Additionally, you can scale up the workers and still provide some benefits, such as general redundancy in case one celery worker container has an issue and stops working. To modify the number of worker instances, update the `AIRFLOW_CELERY_INSTANCES` value in the [.env](.env) file.

If you do want to scale up the workers in the [docker-compose.yml](./docker-compose.yml), simply copy/paste the `airflow-celery-worker` service definition, and rename the services/`container_name` to `airflow-celery-worker-1`, `airflow-celery-worker-2`, etc (for example).

If you have no desire to run the `CeleryExecutor`, or want to reduce your container footprint (as running `CeleryExecutor` adds at least 1 extra container, not including the `airflow-celery-flower` service, explained below), you can instead choose to use the `LocalExecutor`: [https://airflow.apache.org/docs/apache-airflow/stable/executor/local.html](https://airflow.apache.org/docs/apache-airflow/stable/executor/local.html). In order to do this, you would simply need to:

1. Change the `AIRFLOW__CORE__EXECUTOR` environment variable to `LocalExecutor`.
2. Comment out or delete the `redis`, `airflow-celery-worker`, and `airflow-celery-flower` services defined in the [docker-compose.yml](./docker-compose.yml).
3. Remove the `redis` condition from the `depends_on` section of the `airflow-scheduler` service in the [docker-compose.yml](./docker-compose.yml).

**Configurations (`DAGs`)**

When the `AIRFLOW_UPDATE_CONFIG_VARIABLE` environment variable is set to `True`, Airflow will create the `DAGs` from the [opensea_monitor_config.json](./opensea-monitor-airflow/config/opensea_monitor_config.json) configuration file. The `monitor_configs` field is an array of configs - each config in this array will create a `DAG` for an individual collection to monitor. A monitor config object may look like the following:
```json
   "monitor_configs": [
      {
        "collection": "BAYC",
        "monitor_enabled": true,
        "interval_ms": 300000,
        "contract_address": "0x0000000000000000000000000000000000000000",
        "event_type": "CREATED",
        "events_limit_each_call": null,
        "max_events_to_gather": null,
        "discord_webhook_tokens": ["000000000/-a1bbb2-ABBccddEEffffGGG"]
      }
    ]
```
The fields are defined as follows:

| field | data type | example | description| Required |
|---|---|---|---|---|
| collection | `string`  |  `BAYC` | Identifier in Airflow GUI for the DAG | Yes | 
| monitor_enabled | `boolean` |  `true`    |  Whether the Airflow scheduler should load this `DAG`. If `false`, it will not be visible in the GUI, and will not run. | Yes |
| interval_ms | `int` | `30000` | How often Airflow should check the opensea for events. It is recommended to not set this value too low (i.e not lower than 30 seconds), as you want to be a good consumer of the API and respect rate limits.  | Yes |  
| contract_address  |  `int` | `0x0000000000000000000000000000000000000000` | The contract address of the NFT collection to monitor. | Yes |
| event_type | `string` | `CREATED`, `SUCCESSFUL`  | `CREATED` will monitor for new listings. `SUCCESSFUL` will monitor for sold listings| Yes |  
| events_limit_each_call | `int` |  `10` | How many events should be retrived from the OpenSea api for each paged request | No |
| max_events_to_gather | `int` | `50` | Very active collections can have hundreds of events in between poll intervals. Attempting to gather all every time can cause issues. This setting allows you to limit how many events you gather each call. | No |
| discord_webhook_tokens | `[]string` | `["000000000/-a1bbb2-ABBccddEEffffGGG"]`   | List of discord webhooks to send notifications to when new events are found. This is only the token. For example, if the following is the full webhook provided from Discord: `https://discord.com/api/webhooks/000000000/-a1bbb2-ABBccddEEffffGGG`, the token is only the `000000000/-a1bbb2-ABBccddEEffffGGG` part  | Not required, but this field should have at least one webhook, otherwise you will not receive notifications when new events occur. |

Given these requirements, the most basic monitor configuration/`DAG` may look like the following:
```json
      {
         "collection": "BAYC",
         "monitor_enabled": true,
         "interval_ms": 300000,
         "contract_address": "0x0000000000000000000000000000000000000000",
         "event_type": "CREATED"
      }
```

**Creating/Updating Configurations (`DAGs`)**

These configurations can be created and updated, on the fly, by modifying the `opensea_monitor_config` found in the Airflow GUI at `Admin` --> `Variables`. Here you can change the `interval_ms` to make the checks occur more often, or not as often, and Airflow will automatically reload the configuration the next time the `DAG` is considered for scheduling.

**IMPORTANT:** if you update the configurations in this manner, and then later restart Airflow with the `AIRFLOW_UPDATE_CONFIG_VARIABLE` set to `True`, any changes will be overwritten, unless you also updated the master [opensea_monitor_config.json](./opensea-monitor-airflow/config/opensea_monitor_config.json) file to reflect the same changes. If you set `AIRFLOW_UPDATE_CONFIG_VARIABLE` to `False` after initial setup, any values you update in the GUI will not be overwritten if you have to restart the Airflow service. However, it is recommended to take frequent backups of your configuration settings and store them somewhere safe in case you ever need to start from scratch again.

---

###opensea-monitor-api

opensea-monitor-api is the core app that handles the logic for checking opensea for new events. When an [Airflow](./opensea-monitor-airflow) `DAG` is scheduled and runs, `Airflow` calls the `opensea-monitor-api` in order to process that `DAG` run.