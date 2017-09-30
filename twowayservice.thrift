namespace java tutorial.example.twoway

service serverService {
    string repeatMessage(1:i32 interval),
    void stopMessage(),
}

service clientService {
    void receiveMessage(1:string message),
}