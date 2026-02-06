// admin-chat.js — CLEAN & SAFE

const contextPath = '/trainticket/';
const adminId = document.getElementById('adminId')?.value;

let stompClient = null;
let currentConversationId = null;
let selectedFile = null;
let typingTimeout = null;
let lastSenderId = null;
let subscriptions = [];

/* ================= CONNECT ================= */

function connect() {
    const socket = new SockJS(contextPath + 'ws-chat');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, () => {
        loadConversations();

        stompClient.subscribe(
            `/queue/admin/${adminId}/notification`,
            () => loadConversations()
        );
    });
}

/* ================= CONVERSATIONS ================= */

function loadConversations() {
    fetch(contextPath + 'api/admin/conversations')
        .then(r => r.json())
        .then(renderConversationList);
}

function renderConversationList(list) {
    const container = document.getElementById('conversationsList');
    container.innerHTML = '';

    if (!list.length) {
        container.innerHTML = '<p class="p-4 text-gray-500">Không có hội thoại</p>';
        return;
    }

    list.forEach(c => {
        const div = document.createElement('div');
        div.className = `p-4 border-b cursor-pointer ${c.id === currentConversationId ? 'bg-blue-100' : ''}`;
        div.innerHTML = `
            <div class="font-semibold">${c.userName}</div>
            <div class="text-xs text-gray-500">${c.status}</div>
        `;
        div.onclick = () => {
            if (c.status === 'WAITING') {
                if (confirm('Nhận hội thoại này?')) claimConversation(c.id);
            } else {
                openConversation(c.id);
            }
        };
        container.appendChild(div);
    });
}

/* ================= OPEN / CLAIM ================= */

function claimConversation(id) {
    fetch(contextPath + `api/admin/conversation/${id}/claim`, { method: 'POST' })
        .then(() => openConversation(id));
}

function openConversation(id) {
    currentConversationId = id;

    document.getElementById('emptyState').classList.add('hidden');
    document.getElementById('chatContent').classList.remove('hidden');

    unsubscribeAll();

    subscriptions.push(
        stompClient.subscribe(`/topic/conversation/${id}`, m => {
            displayMessage(JSON.parse(m.body));
        })
    );

    subscriptions.push(
        stompClient.subscribe(`/topic/conversation/${id}/typing`, m => {
            const d = JSON.parse(m.body);
            if (d.userId !== Number(adminId)) showTyping(d.isTyping);
        })
    );

    loadMessages(id);
}

/* ================= MESSAGES ================= */

function loadMessages(id) {
    fetch(contextPath + `api/chat/${id}/messages`)
        .then(r => r.json())
        .then(list => {
            const box = document.getElementById('messagesContainer');
            box.innerHTML = '';
            lastSenderId = null;
            list.forEach(displayMessage);
            scrollBottom();
        });
}

function displayMessage(m) {
    const box = document.getElementById('messagesContainer');
    const isOwn = m.senderId == adminId;
    const wrap = document.createElement('div');
    wrap.className = `message ${isOwn ? 'sent' : 'received'}`;

    wrap.innerHTML = `
        <div class="message-bubble px-4 py-2 rounded-lg max-w-md">
            ${m.content || ''}
            ${m.fileUrl ? `<br><a target="_blank" href="${contextPath + m.fileUrl}">📎 ${m.fileName}</a>` : ''}
        </div>
        <div class="text-xs text-gray-400">${formatTime(m.createdAt)}</div>
    `;
    box.appendChild(wrap);
    box.scrollTop = box.scrollHeight;
//    scrollBottom();
}

function sendMessage() {
    if (!currentConversationId) return;

    const input = document.getElementById('messageInput');
    const content = input.value.trim();
    if (!content && !selectedFile) return;

    const fd = new FormData();
    fd.append('content', content);
    if (selectedFile) fd.append('file', selectedFile);

    fetch(contextPath + `api/chat/${currentConversationId}/send`, {
        method: 'POST',
        body: fd
    }).then(() => {
        input.value = '';
        clearFile();
    });
}

/* ================= CLOSE ================= */

function closeConversation() {
    if (!currentConversationId) return;

    if (!confirm('Đóng hội thoại?')) return;

    fetch(contextPath + `api/admin/conversation/${currentConversationId}/close`, {
        method: 'POST'
    }).then(() => {
        currentConversationId = null;
        document.getElementById('chatContent').classList.add('hidden');
        document.getElementById('emptyState').classList.remove('hidden');
        loadConversations();
    });
}

/* ================= UTILS ================= */

function unsubscribeAll() {
    subscriptions.forEach(s => s.unsubscribe());
    subscriptions = [];
}

function scrollBottom() {
    const box = document.getElementById('messagesContainer');
    box.scrollTop = box.scrollHeight;
}

function showTyping(show) {
    document.getElementById('typingIndicator').style.display = show ? 'block' : 'none';
}

function clearFile() {
    selectedFile = null;
    document.getElementById('filePreview').classList.add('hidden');
}

function formatTime(t) {
    return new Date(t).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
}

/* ================= EVENTS ================= */

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('sendButton')?.addEventListener('click', sendMessage);
    document.getElementById('messageInput')?.addEventListener('keypress', e => {
        if (e.key === 'Enter') sendMessage();
    });
    document.getElementById('closeConversationButton')?.addEventListener('click', closeConversation);
});

connect();
