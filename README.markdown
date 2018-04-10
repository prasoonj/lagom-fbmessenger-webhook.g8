A [Giter8][g8] template for creating a Lagom based microservice that acts as a Facebook Messenger Webhook!

# Usage

```
$ sbt new prasoonj/lagom-fbmessenger-webhook.g8
[info] Set current project to test (in build file:/Users/prasoonjoshi/code/test/)

The next big ChatBot.

name [Hello]: chatbot  
organization [com.example]:
version [1.0-SNAPSHOT]:
package [com.example.chatbot]:
facebookVerificationToken [somecomplicatedtoken]: secrettoken

Template applied in ./chatbot

$ tree -L 3
.
├── chatbot
│   ├── build.sbt
│   ├── project
│   │   ├── build.properties
│   │   └── plugins.sbt
│   ├── webhooks-api
│   │   └── src
│   └── webhooks-impl
│       └── src
└── target
    └── streams
        └── $global

9 directories, 3 files

$ cd chatbot
$ sbt runAll

```

Template license
----------------
Written in 2018 by Prasoon Joshi prasoonj@gmail.com
[other author/contributor lines as appropriate]

To the extent possible under law, the author(s) have dedicated all copyright and related
and neighboring rights to this template to the public domain worldwide.
This template is distributed without any warranty. See <http://creativecommons.org/publicdomain/zero/1.0/>.

[g8]: http://www.foundweekends.org/giter8/
