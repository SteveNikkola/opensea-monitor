import json
import logging
from datetime import datetime, timedelta

from airflow import DAG
from airflow.models import Variable
from airflow.operators.python import PythonOperator

from tasks.opensea_monitor_task import check_opensea_for_events

from utils.constant import \
    CONNECTION_CONFIG, \
    MONITOR_CONFIGS, \
    INTERVAL_MS, \
    EVENTS_LIMIT_EACH_CALL, \
    MAX_EVENTS_TO_GATHER, \
    CONTRACT_ADDRESS, \
    COLLECTION, \
    EVENT_TYPE,\
    MONITOR_ENABLED, \
    APP_BASE_URL, \
    MONITOR_PATH, \
    DISCORD_WEBHOOK_TOKENS

logger = logging.getLogger(__name__)


def configure_dag(
        dag_id,
        default_args,
        connection_config,
        monitor_config):
    dag = DAG(
        dag_id,
        description=dag_id,
        start_date=datetime(2020, 10, 1),
        end_date=datetime(2222, 10, 1),
        catchup=False,
        schedule_interval=timedelta(milliseconds=monitor_config[INTERVAL_MS]),
        max_active_runs=1,
        default_args=default_args
    )

    with dag:
        check_for_changes_task = PythonOperator(
            task_id=f'check_for_events_task_opensea-monitor_{monitor_config[COLLECTION]}',
            python_callable=check_opensea_for_events,
            op_kwargs={
                CONTRACT_ADDRESS: monitor_config[CONTRACT_ADDRESS],
                EVENT_TYPE: monitor_config[EVENT_TYPE],
                APP_BASE_URL: connection_config[APP_BASE_URL],
                MONITOR_PATH: connection_config[MONITOR_PATH],
                EVENTS_LIMIT_EACH_CALL: monitor_config[EVENTS_LIMIT_EACH_CALL],
                MAX_EVENTS_TO_GATHER: monitor_config[MAX_EVENTS_TO_GATHER],
                DISCORD_WEBHOOK_TOKENS: monitor_config[DISCORD_WEBHOOK_TOKENS]
            },
            dag=dag
        )

        check_for_changes_task

    return dag


def configure_master_dag():
    monitor_configs = Variable.get('opensea_monitor_config', default_var={})
    monitor_configs = json.loads(monitor_configs)

    # build dags for each store we want to monitor
    for monitor_config in monitor_configs[MONITOR_CONFIGS]:
        if monitor_config[MONITOR_ENABLED]:
            dag_id = f'opensea_monitor_{monitor_config[COLLECTION]}'

            logger.info(f'Configuring Opensea Monitor DAG: {dag_id}')

            default_args = {
                'retries': 0,
                'provide_context': False,
            }

            globals()[dag_id] = configure_dag(
                dag_id=dag_id,
                default_args=default_args,
                connection_config=monitor_configs[CONNECTION_CONFIG],
                monitor_config=monitor_config
            )


configure_master_dag()
