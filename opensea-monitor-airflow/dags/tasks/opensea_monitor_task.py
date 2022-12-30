import logging
import urllib3
import json

from utils.constant import \
    EVENTS_LIMIT_EACH_CALL, \
    MAX_EVENTS_TO_GATHER, \
    EVENT_TYPE, \
    CONTRACT_ADDRESS, \
    APP_BASE_URL, \
    MONITOR_PATH, \
    DISCORD_WEBHOOK_TOKENS, \
    COLLECTION_EVENTS_MONITOR_PATH

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
http = urllib3.PoolManager()

logger = logging.getLogger(__name__)


def check_opensea_for_events(**kwargs):
    logger.info(f"Checking for '{EVENT_TYPE}' events for '{CONTRACT_ADDRESS}")

    url = f'http://{kwargs[APP_BASE_URL]}/{kwargs[MONITOR_PATH]}/{COLLECTION_EVENTS_MONITOR_PATH}'

    post_body = {
        'contract_address': kwargs[CONTRACT_ADDRESS],
        'event_type': kwargs[EVENT_TYPE],
        'limit': kwargs[EVENTS_LIMIT_EACH_CALL],
        'max_events_to_gather': kwargs[MAX_EVENTS_TO_GATHER],
        'discord_web_tokens': kwargs[DISCORD_WEBHOOK_TOKENS]
    }

    logger.info('URL to call: {url}'.format(url=url))
    request = http.request('POST', url, headers={'Content-Type': 'application/json'}, body=json.dumps(post_body))

    http_status = request.status
    if http_status != 200:
        raise Exception(f'Bad status code received from monitor: {http_status}')
