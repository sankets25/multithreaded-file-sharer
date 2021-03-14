# Multi-threaded file sharer

A client-server architecture based multi-threaded file sharing distributed system in Java.

• Client chooses a unique username  
• Server forks a thread for every client which connects  
• Client adds new files to his/her the sending directory  
• Other clients receive the new file, or an update if the file exists

<p align=justify>Client code continuously scans/watches the directory for new content. If found new content, then the content is forwarded to clients connected. If an update to a file is provided, then the other clients have the option to accept or decline the update.</p>
