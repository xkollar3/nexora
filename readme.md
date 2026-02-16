# Nexora
Platforma na správu dát inšpirovaná Datify. Prispôsobená AI asistentom cez MCP.
Stack: Docker + Java + React

## Backend
Backend je napísaný v Jave 25 s použitím Spring Boot modulitu.
- Podporuje vytváranie schém a základné operácie nad dátami v schéme.
- Obsahuje MCP nástroje pre AI agentov.
- Event-driven architektúra v modulite, funkcionalita je implementovaná ako vertical slices.
- Obsahuje jeden agregát, ktorý spracováva business logiku.
- Event-driven prístup drží aplikácie loosely coupled a dovoľuje ľahko implementovať patterny, ktoré reagujú na udalosti v doméne. Pôvodne som zamýšľal tzv. „guardian agent", akurát to vyšlo mimo scope a nebol čas.
- Ukladá dáta v MongoDB kvôli flexibilite dokumentového modelu (nie je nutné vytvárať schémy cez SQL).
- WebSocket posiela von eventy, aby bola aplikácia reaktívna.
- CQRS aplikácia, prijíma commandy, ktoré menia stav dát. Read strana aplikácie si vytvára ľubovoľné potrebné modely. Separácia dovoľuje spravovanie týchto funkcionalít oddelene.

## Frontend
- Iba na ukážku, naimplementovaný cez spec-driven development a vibe coding.
- React + TanStack + Axios + Zod, StompJS WebSocket, shadcn + Tailwind styling.

## Spustenie
```
docker compose up
mvn spring-boot:run
npm i && npm run dev
```
