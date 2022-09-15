<h1>Chat Room</h1>
<p>This is a chat room test program. People only who had an account and password can use.</p>
<h2>requirements analysis</h2>
<p>This program has the following function:</p>
<p><i>The string startwith "@" is a command in this program.</i></p>

- User can connect to the server and ask server the onsite userList.
  Using @ListU to list all user onsite.
  
- User can then can select another onsite user to talk.
  Using @To [username] to set current chat partner.

- User talking can only use simple text now.

- User can receive any other user's messages, but also you need using @To command to reply.

- User can exit chat with some other user. 
  Using @Bye to set current chat partner to default null;

- User can disconnect to the server at anytime. 
  Using @Exit command to exit.
  
- When your current chat partner is disconnect with the server, you will get a hint. 

- The server will say hello to all users connecting to it.
  
- The server will maintain an onsite user list.

- The server will transfer the user's message from one to another.

- The server will remove the user from onsite user list when he disconnected.

- The server will maintain a chat session list which include <from, to> info for user chat.

<h2>class define</h2>
User:<br>
- userId
- userName
- socket
- talk(to, message)
- talk(message)
- bye()
- exit()

Server:
- List<String> onsiteUser
- Map<String, String> user talk partner list
- ServerSocket
- handleEvent
- handleCommand
- handleMessage