
CLASSPATH= -classpath "lib/gson-2.2.4.jar:lib/commons-codec-1.8.jar:."

JC = javac
.SUFFIXES: .java .class
.java.class:
		$(JC) $(CLASSPATH) $*.java

CLASSES = KeyAndValue.java\
        HashUtility.java\
        ICommand.java\
        Constants.java\
        Key.java\
        KVCommands.java\
        KVData.java\
        KVDataWithSrcHost.java\
        KVStore.java\
        StatusCode.java\
        KVClientResponse.java\
        KVClientShowResponse.java\
        MarshalledClientData.java\
        MarshalledServerData.java\
        TableEntry.java\
        LocalCommand.java\
        DirectLocalCommand.java\
        DirectLocalShowCommand.java\
        IndirectLocalCommand.java\
        RemoteCommand.java\
        IndirectLocalCommandNoCallback.java\
        RemoteMoveBulkCommand.java\
        GrepProtocol.java \
        TestGrepProtocol.java \
        TestClientGrep.java \
        GossipTimeOutManager.java\
        GossipTransmitter.java\
        GossipReceiver.java\
        GrepServer.java \
        GrepClient.java \
        KVClientRequestServer.java\
        KVServer.java\
        KVClientAPI.java\
        KVClient.java\
        GossipServer.java

default: classes


classes: $(CLASSES:.java=.class)

clean:
		$(RM) *.class
