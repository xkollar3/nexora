import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import { useQueryClient } from '@tanstack/react-query';

interface RecordChangeEvent {
  type: 'INSERTED' | 'UPDATED' | 'DELETED';
  recordId: string;
  schemaId: string;
  occurredAt: string;
}

export function useRecordEvents(schemaIds: string[]) {
  const queryClient = useQueryClient();
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (schemaIds.length === 0) return;

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const brokerURL = `${protocol}//${window.location.host}/ws`;

    const client = new Client({
      brokerURL,
      reconnectDelay: 5000,
      onConnect: () => {
        for (const schemaId of schemaIds) {
          client.subscribe(`/topic/schemas/${schemaId}`, (message) => {
            const event: RecordChangeEvent = JSON.parse(message.body);
            queryClient.invalidateQueries({ queryKey: ['records'] });

            if (event.type === 'INSERTED' || event.type === 'DELETED') {
              queryClient.invalidateQueries({ queryKey: ['record-schemas'] });
            }
          });
        }
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };
  }, [schemaIds.join(','), queryClient]);
}
