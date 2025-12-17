import pika
import json
import time
import random
from datetime import datetime, timedelta

CLOUDAMQP_URL = 'amqps://pzmgmmeq:vrQJsWZakcDjjwcPOxv8FHRzUssr5qeU@collie.lmq.cloudamqp.com/pzmgmmeq'
DEVICE_ID = "2f2077e8-7565-436e-87c3-54d5dfb186d6" 
TARGET_DATE_STR = "2025-12-18" 

EXCHANGE_NAME = '' 
ROUTING_KEY = 'sensor.data.central' 

def get_rabbitmq_connection():
    params = pika.URLParameters(CLOUDAMQP_URL)
    return pika.BlockingConnection(params)

def generate_value_for_hour(hour):
    return 5000.0
#    """Generates realistic consumption based on the time of day."""
#    if 0 <= hour < 6:
#        base = 0.2  # Night
#    elif 6 <= hour < 10:
#        base = 1.0  # Morning
#    elif 10 <= hour < 17:
#        base = 0.6  # Work hours
#    elif 17 <= hour < 22:
#        base = 2.5  # Evening peak
#    else:
#        base = 0.5  # Late night
#    val = base + random.uniform(-0.1, 0.1) * base
#    return round(max(0.05, val), 2)

def main():
    print(f"--- GENERATOR -> LOAD BALANCER ---")
    print(f"Target Queue: {ROUTING_KEY}")

    try:
        connection = get_rabbitmq_connection()
        channel = connection.channel()
        channel.queue_declare(queue=ROUTING_KEY, durable=True)

        start_time = datetime.strptime(TARGET_DATE_STR, "%Y-%m-%d")
        end_time = start_time + timedelta(days=1)
        current_time = start_time
        count = 0

        while current_time < end_time:
            timestamp = int(current_time.timestamp() * 1000)
            value = generate_value_for_hour(current_time.hour)

            message_payload = {
                "timestamp": timestamp,
                "deviceId": DEVICE_ID,
                "measurementValue": value
            }

            channel.basic_publish(
                exchange=EXCHANGE_NAME,
                routing_key=ROUTING_KEY,
                body=json.dumps(message_payload),
                properties=pika.BasicProperties(content_type='application/json')
            )

            time.sleep(0.05)
            
            current_time += timedelta(hours=1)
            count += 1
            
        print(f"\n[SUCCESS] Sent {count} messages to Central Queue '{ROUTING_KEY}'.")
        connection.close()

    except Exception as e:
        print(f"Error: {e}")

if __name__ == '__main__':
    main()
