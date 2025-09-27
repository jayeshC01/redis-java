# Redis-Java

A simple, educational Redis-like in-memory datastore implemented in Java. Inspired by [Redis](https://redis.io/), this project aims to demonstrate core Redis concepts such as RESP protocol, Redis Commands, and concurrent client handling.

CodeCrafter Profile: https://app.codecrafters.io/users/jayeshC01

---

## Highlights

- **Multi-client Support**: Handles multiple concurrent TCP clients.
- **RESP Protocol**: Fully parses and responds using the [Redis Serialization Protocol (RESP)](https://redis.io/docs/reference/protocol-spec/).
- **Thread-safe In-memory Store**: Uses concurrent data structures.
- **Easy Local Testing**: Run and test with common tools (`nc`, `telnet`).
- **Extensible Command Processing**: Modular design for adding new commands.
- **Stream and RDB support**: Includes stream commands and basic config handling.

---

## Local Testing & Usage

1. **Build & Run**
   ```sh
   mvn clean package
   ./your_program.sh
   ```
   By default, the server listens on port `6379`.

   **RDB Persistence:**  
   You can specify the directory and DB filename for RDB persistence by passing arguments:
   ```sh
   ./your_program.sh --dir <directory_path> --dbfilename <filename>
   ```
   Example:
   ```sh
   ./your_program.sh --dir ./data --dbfilename db.rdb
   ```

2. **Sending Commands (RESP Format Required!)**

   This server expects commands in RESP format.  
   Example: The command `ECHO hi` must be sent as:
   ```
   *2\r\n$4\r\nECHO\r\n$2\r\nhi\r\n
   ```

   **Send via netcat:**
   ```sh
   printf '*2\r\n$4\r\nECHO\r\n$2\r\nhi\r\n' | nc -v localhost:6379
   ```

   For details on RESP, see:  
   [Redis Protocol Specification](https://redis.io/docs/reference/protocol-spec/)

---

## Implemented Command List

| Category        | Commands                                                                                                 |
|-----------------|---------------------------------------------------------------------------------------------------------|
| Basic           | `PING`, `ECHO`                                                                                          |
| Strings         | `SET`, `GET`, `INCR`                                                                                    |
| Lists           | `LPUSH`, `RPUSH`, `LLEN`, `LRANGE`, `LPOP`, `BLPOP`                                                     |
| Transactions    | `MULTI`, `EXEC`, `DISCARD`                                                                              |
| Streams         | `XADD`, `XRANGE`, `XREAD`                                                                               |
| Other           | `TYPE`, `CONFIG`                                                                                        |

---

## Project Structure

- `src/main/java/Main.java` — Entry point, TCP server.
- `src/main/java/processors/` — Command implementations.
- `src/main/java/db/` — In-memory data store.

---

## License

For educational use only.