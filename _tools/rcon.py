#!/usr/bin/env python3
"""Tiny RCON client so the test server can be driven without a Minecraft client."""
import socket, struct, sys, time

HOST, PORT, PASSWORD = '127.0.0.1', 25575, 'test'


def packet(rid, ptype, body):
    payload = struct.pack('<ii', rid, ptype) + body.encode('utf8') + b'\x00\x00'
    return struct.pack('<i', len(payload)) + payload


def read_packet(sock):
    raw = sock.recv(4)
    if len(raw) < 4:
        return None, None, None
    length = struct.unpack('<i', raw)[0]
    data = b''
    while len(data) < length:
        chunk = sock.recv(length - len(data))
        if not chunk:
            break
        data += chunk
    rid, ptype = struct.unpack('<ii', data[:8])
    return rid, ptype, data[8:-2].decode('utf8', 'replace')


def run(commands):
    sock = socket.create_connection((HOST, PORT), timeout=10)
    sock.sendall(packet(1, 3, PASSWORD))
    rid, ptype, body = read_packet(sock)
    if rid == -1:
        raise SystemExit('rcon auth failed')
    out = []
    for i, cmd in enumerate(commands, start=2):
        sock.sendall(packet(i, 2, cmd))
        rid, ptype, body = read_packet(sock)
        out.append(body)
    sock.close()
    return out


if __name__ == '__main__':
    for cmd, response in zip(sys.argv[1:], run(sys.argv[1:])):
        print('>', cmd)
        print(response)
