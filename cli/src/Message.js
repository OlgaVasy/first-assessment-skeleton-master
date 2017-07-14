export class Message {
  static fromJSON (buffer) {
    return new Message(JSON.parse(buffer.toString()))
  }

  constructor ({ type, time, username, host, port, command, contents }) {
    this.username = username
    this.host = host
    this.port = port
    this.command = command
    this.contents = contents
    this.time = time
    this.type=type


  }

  toJSON () {
    return JSON.stringify({
      username: this.username,
      command: this.command,
      contents: this.contents,
      time: this.time,
      type: this.type


    })
  }

  toString () {
    return this.time + this.type+ ' ' + '<'+this.username+'> ' + this.contents
  }
}
