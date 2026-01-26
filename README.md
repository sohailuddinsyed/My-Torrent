# My-Torrent
A decentralized file distribution application implementing BitTorrent-inspired protocols for efficient peer-to-peer transfers. Supports files up to 1GB with intelligent peer selection and choking algorithms.

## Getting Started
- Extract the archive and enter the `src` folder
- Build using `javac *.java` (requires OpenJDK 17.0.8.1+)
- Launch on each machine with `./compileJava`
  
## System Architecture

**peerProcess.java**
- Initializes peer configuration from Common.cfg and PeerInfo.cfg
- Manages peer registry using PeerDetails data structures
- Configures bitfield representation for file piece tracking
- Instantiates FileHandler for splitting files into transferable chunks
- Spawns client/server threads for connection management
- Orchestrates neighbor selection through SelectNeighbors and SelectOptNeighbor modules

**PeerDetails.java**
- Encapsulates individual peer state including ID, port, availability status, bitfield, and socket information
- Dynamically updates based on network communication

**PeerClient.java**
- Initiates TCP connections to preceding peers from PeerInfo.cfg
- Performs handshake protocol validation via Handshake.java
- Caches connection streams in peer objects
- Exchanges bitfield information upon successful authentication
- Delegates ongoing message processing to P2PMessageHandler

**PeerServer.java**
- Listens for inbound peer connections
- Validates incoming handshakes and responds accordingly
- Broadcasts local bitfield to new connections
- Launches P2PMessageHandler for post-handshake communication

**Message.java**
- Constructs protocol-compliant message objects
- Provides utilities for serialization, type identification (via MessageType.java), and payload extraction

**P2PMessageHandler.java**
- Central message routing and processing engine
- Implements type-specific handlers for all protocol messages
- Leverages Utils.java for transmission, interest evaluation, piece selection, and completion detection
- Coordinates with FileHandler.java for piece storage and file reconstruction

**Logger.java**
- Per-peer logging system tracking network events and file transfer activities

**SelectNeighbors.java & SelectOptNeighbor.java**
- Implements preferred neighbor selection based on download rates or random selection during initialization
- Manages optimistic unchoking by randomly selecting interested choked peers
`
