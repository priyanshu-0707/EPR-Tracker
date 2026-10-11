import React, { useState } from 'react';
import { aiService } from '../../services';
import { Skeleton } from '../ui/Skeleton';

interface Message {
  role: 'user' | 'assistant';
  content: string;
  timestamp: string;
}

export const AiAssistantDrawer: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState<Message[]>([
    {
      role: 'assistant',
      content: 'Namaste! I am your CPCB E-Waste Rules 2022 assistant. Ask me anything regarding your EPR obligation formula, recycling certificates, or deadlines.',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    },
  ]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const suggestedQuestions = [
    'What is my EPR obligation for 5 lakh phones?',
    'What happens if I miss the quarterly filing?',
    'Which recyclers are CPCB-certified in Maharashtra?',
  ];

  const handleSend = async (textToSend?: string) => {
    const query = textToSend || input;
    if (!query.trim() || isLoading) return;

    const userMsg: Message = {
      role: 'user',
      content: query,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages((prev) => [...prev, userMsg]);
    if (!textToSend) setInput('');
    setIsLoading(true);

    try {
      const res = await aiService.askAssistant(query);
      const botMsg: Message = {
        role: 'assistant',
        content: res.reply,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, botMsg]);
    } catch {
      const errorMsg: Message = {
        role: 'assistant',
        content: 'Unable to reach Bedrock assistant at this time. Please check network connection.',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleClear = () => {
    setMessages([
      {
        role: 'assistant',
        content: 'Chat session cleared. How may I assist your compliance team today?',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      },
    ]);
  };

  return (
    <>
      {/* Floating Action Button */}
      <button
        onClick={() => setIsOpen(true)}
        className="fixed bottom-6 right-6 z-40 flex items-center gap-2 px-4 py-3 bg-purple text-bg rounded-full shadow-2xl hover:bg-purple/90 transition-all font-medium text-sm border border-purple/40 group hover:scale-105"
      >
        <span className="text-lg">⚡</span>
        <span className="font-semibold tracking-wide">Ask EPR AI</span>
      </button>

      {/* Drawer Overlay */}
      {isOpen && (
        <div
          onClick={() => setIsOpen(false)}
          className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm transition-opacity"
        />
      )}

      {/* Drawer Container (380px wide) */}
      <div
        className={`fixed top-0 right-0 z-50 h-full w-full max-w-[380px] bg-surface border-l border-border flex flex-col shadow-2xl transform transition-transform duration-300 ease-in-out ${
          isOpen ? 'translate-x-0' : 'translate-x-full'
        }`}
      >
        {/* Drawer Header */}
        <div className="flex items-center justify-between px-4 py-3.5 border-b border-border bg-surface-2/60">
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-purple animate-ping" />
            <h3 className="font-semibold text-sm text-text">EPR Legal Assistant</h3>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={handleClear}
              className="text-xs text-text-muted hover:text-text px-2 py-1 rounded hover:bg-surface-2 transition-colors"
            >
              Clear
            </button>
            <button
              onClick={() => setIsOpen(false)}
              className="text-text-muted hover:text-text text-xl leading-none p-1"
            >
              &times;
            </button>
          </div>
        </div>

        {/* Suggested Queries */}
        <div className="p-3 bg-surface-2/30 border-b border-border/60">
          <p className="text-[11px] text-text-muted uppercase font-mono tracking-wider mb-2">
            Suggested Regulations
          </p>
          <div className="flex flex-col gap-1.5">
            {suggestedQuestions.map((q, idx) => (
              <button
                key={idx}
                onClick={() => handleSend(q)}
                className="text-left text-xs text-text-muted hover:text-purple hover:bg-surface-2 px-2.5 py-1.5 rounded transition-colors truncate border border-border/40"
              >
                {q}
              </button>
            ))}
          </div>
        </div>

        {/* Message Log */}
        <div className="flex-1 overflow-y-auto p-4 flex flex-col gap-3">
          {messages.map((m, idx) => (
            <div
              key={idx}
              className={`flex flex-col max-w-[85%] ${
                m.role === 'user' ? 'ml-auto items-end' : 'mr-auto items-start'
              }`}
            >
              <div
                className={`p-3 rounded-card text-xs leading-relaxed whitespace-pre-wrap ${
                  m.role === 'user'
                    ? 'bg-blue text-bg font-medium'
                    : 'bg-surface-2 border border-border text-text'
                }`}
              >
                {m.content}
              </div>
              <span className="text-[10px] text-text-dim mt-1 font-mono">{m.timestamp}</span>
            </div>
          ))}

          {isLoading && (
            <div className="mr-auto w-4/5 flex flex-col gap-2 p-3 bg-surface-2 border border-border rounded-card">
              <Skeleton className="h-3 w-3/4" />
              <Skeleton className="h-3 w-full" />
              <Skeleton className="h-3 w-1/2" />
            </div>
          )}
        </div>

        {/* Chat Input */}
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSend();
          }}
          className="p-3 border-t border-border bg-surface-2/50 flex gap-2"
        >
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Type query under CPCB Rules..."
            className="flex-1 bg-surface border border-border rounded-input px-3 py-2 text-xs text-text placeholder-text-dim focus:outline-none focus:border-purple"
          />
          <button
            type="submit"
            disabled={isLoading || !input.trim()}
            className="bg-purple text-bg px-3 py-2 rounded-input font-medium text-xs hover:bg-purple/90 disabled:opacity-50 transition-colors"
          >
            Send
          </button>
        </form>
      </div>
    </>
  );
};
