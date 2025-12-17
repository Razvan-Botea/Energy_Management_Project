import React, { useState, useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs'; //
import { useAuth } from '../context/AuthContext';

const ChatComponent = () => {
    const auth = useAuth();
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [isOpen, setIsOpen] = useState(false);
    const [isTyping, setIsTyping] = useState(false);
    
    const messagesEndRef = useRef(null);

    useEffect(() => {
        const client = new Client({
            brokerURL: 'ws://localhost:8000/ws', 
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        client.onConnect = (frame) => {
            console.log("Subscribing to topic: /topic/chat/" + auth.userId);
            client.subscribe(`/topic/chat/${auth.userId}`, (message) => {
                const receivedMsg = JSON.parse(message.body);
                console.log("RECEIVED ON PUBLIC:", receivedMsg);
                if (receivedMsg.isFromAdmin) {
                    setMessages((prev) => [...prev, receivedMsg]);
                    setIsTyping(false);
                }
            });
        };

        client.onStompError = (frame) => {
            console.error('Broker reported error: ' + frame.headers['message']);
        };

        client.activate();

        return () => {
            client.deactivate();
        };
    }, [auth.userId]);

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages]);

    const sendMessage = async () => {
        if (!input.trim()) return;

        const payload = {
            senderId: auth.userId,
            content: input,
            isFromAdmin: false,
            timestamp: Date.now()
        };

        setMessages(prev => [...prev, payload]);
        
        setIsTyping(true); 
        setInput('');

        try {
            await fetch('http://localhost:8000/api/chat/send', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${auth.token}`
                },
                body: JSON.stringify(payload)
            });
        } catch (error) {
            console.error("Error sending message:", error);
            setIsTyping(false);
            setMessages(prev => [...prev, { content: "Failed to send message", isFromAdmin: true, isError: true }]);
        }
    };

    return (
        <div style={{ position: 'fixed', bottom: '20px', right: '20px', zIndex: 1000 }}>
            {/* Toggle Button */}
            {!isOpen && (
                <button 
                    onClick={() => setIsOpen(true)}
                    style={{ 
                        width: '60px', height: '60px', borderRadius: '30px', 
                        backgroundColor: '#007bff', color: 'white', border: 'none', 
                        fontSize: '30px', cursor: 'pointer', boxShadow: '0 4px 8px rgba(0,0,0,0.2)'
                    }}
                >
                    💬
                </button>
            )}

            {/* Chat Window */}
            {isOpen && (
                <div style={{ 
                    width: '350px', height: '500px', backgroundColor: 'white', 
                    borderRadius: '10px', boxShadow: '0 4px 12px rgba(0,0,0,0.3)',
                    display: 'flex', flexDirection: 'column', overflow: 'hidden'
                }}>
                    <div style={{ padding: '15px', background: '#007bff', color: 'white', display: 'flex', justifyContent: 'space-between' }}>
                        <span>Support Assistant</span>
                        <button onClick={() => setIsOpen(false)} style={{ background: 'none', border: 'none', color: 'white', cursor: 'pointer' }}>✖</button>
                    </div>

                    <div style={{ flex: 1, padding: '15px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '10px', background: '#f9f9f9' }}>
                        {messages.map((msg, idx) => (
                            <div key={idx} style={{ 
                                alignSelf: msg.isFromAdmin ? 'flex-start' : 'flex-end',
                                background: msg.isFromAdmin ? '#e9ecef' : '#007bff',
                                color: msg.isFromAdmin ? 'black' : 'white',
                                padding: '10px', borderRadius: '10px', maxWidth: '80%', wordWrap: 'break-word'
                            }}>
                                {msg.content}
                            </div>
                        ))}
                        {isTyping && <div style={{ color: 'gray', fontSize: '0.8em', fontStyle: 'italic' }}>Assistant is typing...</div>}
                        <div ref={messagesEndRef} />
                    </div>

                    <div style={{ padding: '10px', borderTop: '1px solid #ddd', display: 'flex' }}>
                        <input 
                            type="text" 
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
                            placeholder="Type here..."
                            style={{ flex: 1, padding: '8px', border: '1px solid #ddd', borderRadius: '4px', marginRight: '5px' }}
                        />
                        <button onClick={sendMessage} style={{ padding: '8px 15px', background: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Send</button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ChatComponent;
