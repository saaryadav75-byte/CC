# Huffman Compressor

A full-stack web application for compressing and decompressing files using the Huffman coding algorithm. Built with Spring Boot backend and modern HTML/CSS/JavaScript frontend.

## Features

- **File Compression**: Compress any file using Huffman coding algorithm
- **File Decompression**: Decompress previously compressed .huf files
- **Real-time Statistics**: View compression ratio and space saved
- **Drag & Drop Interface**: Modern, responsive web interface
- **File Validation**: Secure file handling with size and type validation
- **Lossless Compression**: Perfect reconstruction of original data

## Technology Stack

### Backend
- **Spring Boot 3.2.0**: Java web framework
- **Java 17**: Programming language
- **Maven**: Dependency management
- **Thymeleaf**: Template engine

### Frontend
- **HTML5**: Markup language
- **Tailwind CSS**: Utility-first CSS framework
- **Vanilla JavaScript**: Client-side functionality

## Project Structure

```
src/
├── main/
│   ├── java/com/huffman/compressor/
│   │   ├── controller/
│   │   │   └── CompressionController.java
│   │   ├── model/
│   │   │   └── HuffmanNode.java
│   │   ├── service/
│   │   │   ├── FileValidationService.java
│   │   │   └── HuffmanService.java
│   │   └── HuffmanCompressorApplication.java
│   └── resources/
│       ├── templates/
│       │   └── index.html
│       └── application.properties
└── test/
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Installation

1. **Clone the repository** (if using version control):
   ```bash
   git clone <repository-url>
   cd huffman-compressor
   ```

2. **Build the project**:
   ```bash
   mvn clean install
   ```

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**:
   Open your web browser and navigate to `http://localhost:8080`

### Alternative: Using IDE

1. Import the project as a Maven project in your favorite IDE (IntelliJ IDEA, Eclipse, etc.)
2. Run the `HuffmanCompressorApplication.java` main class

## Usage

### Compressing Files

1. Open the application in your browser
2. Drag and drop a file into the "Compress File" section or click to browse
3. Click "Compress File" button
4. The compressed file (.huf) will be downloaded automatically
5. View compression statistics showing original size, compressed size, and space saved

### Decompressing Files

1. In the "Decompress File" section, upload a .huf file
2. Click "Decompress File" button
3. The original file will be downloaded with "_decompressed" suffix

## API Endpoints

### Compression
- `POST /compress` - Compress a file
  - Request: Multipart form data with file
  - Response: Downloadable compressed file

### Decompression
- `POST /decompress` - Decompress a file
  - Request: Multipart form data with .huf file
  - Response: Downloadable original file

### Statistics
- `GET /stats` - Get compression statistics
  - Request: Multipart form data with file
  - Response: JSON with compression metrics

## File Support

### Supported File Types
- Text files (.txt)
- Images (.jpg, .png, .gif)
- Documents (.pdf)
- Archives (.zip)
- Any other file types (treated as binary data)

### File Size Limit
- Maximum file size: 50MB
- Configurable in `application.properties`

## Algorithm Details

### Huffman Coding
The application implements the Huffman coding algorithm:

1. **Frequency Analysis**: Count frequency of each byte in the input
2. **Tree Construction**: Build a binary tree based on frequencies
3. **Code Generation**: Generate variable-length codes for each byte
4. **Encoding**: Replace original bytes with their Huffman codes
5. **Decoding**: Use the tree to reconstruct original data

### Compression Efficiency
- Compression ratio varies based on file content
- Text files typically achieve 30-60% compression
- Binary files may have lower compression ratios
- Repetitive data compresses better

## Configuration

### Application Properties
Key configuration options in `src/main/resources/application.properties`:

```properties
# Server port
server.port=8080

# File upload limits
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# Logging
logging.level.com.huffman.compressor=INFO
```

## Development

### Running Tests
```bash
mvn test
```

### Building for Production
```bash
mvn clean package
java -jar target/huffman-compressor-1.0.0.jar
```

## Security Considerations

- File size validation prevents DoS attacks
- File type validation restricts allowed formats
- All processing happens server-side
- No temporary files stored on disk
- Input validation for all endpoints

## Performance

- Memory-efficient streaming for large files
- Optimized Huffman tree construction
- Fast bit-level I/O operations
- Concurrent processing support

## Limitations

- Compression ratio depends on data redundancy
- Very small files may not compress well
- Binary files with high entropy may expand slightly
- Single-threaded processing per request

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is open source and available under the MIT License.

## Troubleshooting

### Common Issues

1. **File upload fails**: Check file size limit and supported formats
2. **Decompression fails**: Ensure file is a valid .huf file
3. **Memory issues**: Increase JVM heap size for large files
4. **Port conflicts**: Change server port in application.properties

### Getting Help

- Check application logs for error details
- Verify Java and Maven versions
- Ensure sufficient disk space and memory
- Test with smaller files first

## Future Enhancements

- Batch file processing
- Progress indicators for large files
- Additional compression algorithms
- Cloud storage integration
- User authentication and file management
