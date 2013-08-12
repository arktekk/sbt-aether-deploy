# Testing can be done by installing Python dav server

    pip install PyWebDAV

Run the following commands in a separate shell

    mkdir /tmp/davroot
    davserver -D /tmp/davroot -H localhost -P 8009 -l INFO -n