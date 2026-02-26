from http.server import HTTPServer, BaseHTTPRequestHandler
import json

class SimpleHTTPRequestHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        # Escuchar peticiones en la ruta "/scores"
        if self.path == '/scores':
            content_length = int(self.headers['Content-Length'])
            post_data = self.rfile.read(content_length)
            
            try:
                # Convertir los datos recibidos a JSON y mostrarlos
                data = json.loads(post_data)
                print(f"\n[SERVIDOR] ¡Nuevo puntaje recibido!")
                print(f"Datos: {data}")
                
                # Responder que todo salió bien (código 200)
                self.send_response(200)
                self.send_header('Content-type', 'application/json')
                self.end_headers()
                self.wfile.write(b'{"status": "recibido"}')
            except Exception as e:
                print(f"Error procesando datos: {e}")
                self.send_response(400)
                self.end_headers()
        else:
            self.send_response(404)
            self.end_headers()

# Configurar el servidor en el puerto 8000
port = 8000
server_address = ('0.0.0.0', port)
httpd = HTTPServer(server_address, SimpleHTTPRequestHandler)

print(f"Servidor de prueba iniciado en el puerto {port}")
print("Esperando puntajes del celular...")
print("Presiona Ctrl+C para detener el servidor.")

httpd.serve_forever()
