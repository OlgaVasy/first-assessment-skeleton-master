import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server
let host
let port
let color='blue'


cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username> [host] [port]')
  .delimiter(cli.chalk['green']('connected>'))
  .init(function (args, callback) {
    username = args.username
    host=args.host
    port=args.port


      server = connect({ host: host, port: port }, () => {
      server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')

      callback()
    })

    server.on('data', (buffer) => {
      this.log(cli.chalk[color](Message.fromJSON(buffer).toString()))
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    const [ command, ...rest ] = words(input)
    const contents = rest.join(' ')

    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')


    } else if (command === 'echo') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
      color= 'yellow'
    }
     else if (command === 'broadcast') {
        server.write(new Message({ username, command, contents }).toJSON() + '\n')

  }
     else if (command === 'users') {
       server.write(new Message({ username, command, contents }).toJSON() + '\n')
    }
     else {
       server.write(new Message({ username, command, contents }).toJSON() + '\n')

    }

    callback()
  })
