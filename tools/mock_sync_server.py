from http.server import BaseHTTPRequestHandler, HTTPServer
import urllib.parse as urlparse

class SyncHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        if self.path != '/syncjava.php':
            self.send_response(404)
            self.end_headers()
            self.wfile.write(b'Not Found')
            return
        length = int(self.headers.get('content-length', 0))
        body = self.rfile.read(length) if length > 0 else b''
        # echo back a simple JSON indicating success
        response = ('{"status":"ok","received":%d}' % len(body)).encode('utf-8')
        self.send_response(200)
        self.send_header('Content-Type', 'application/json; charset=utf-8')
        self.send_header('Content-Length', str(len(response)))
        self.end_headers()
        self.wfile.write(response)

if __name__ == '__main__':
    server_address = ('127.0.0.1', 8000)
    httpd = HTTPServer(server_address, SyncHandler)
    print('Mock sync server running at http://%s:%d/syncjava.php' % server_address)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        httpd.server_close()
        print('\nServer stopped')
