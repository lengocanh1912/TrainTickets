// user-chat.js - Enhanced Version

/* ========== CONFIGURATION ========== */
const conversationId = window.chatConfig.conversationId;
const userId = window.chatConfig.userId;
const contextPath = window.chatConfig.contextPath || '/trainticket';

let stompClient = null;
let typingTimeout = null;
let isConnected = false;
let reconnectAttempts = 0;
let maxReconnectAttempts = 5;
let selectedFile = null;

/* ========== CONNECTION MANAGEMENT ========== */

function connect() {
    try {
        const socket = new SockJS(`${contextPath}/ws-chat`);
        stompClient = Stomp.over(socket);
        stompClient.debug = null; // Disable debug logs

        stompClient.connect({userId: userId },
            () => onConnected(),
            (error) => onConnectionError(error)
        );
    } catch (error) {
        console.error('Connection error:', error);
        updateConnectionStatus(false);
        scheduleReconnect();
    }
}

function onConnected() {
    console.log('WebSocket connected successfully');
    isConnected = true;
    reconnectAttempts = 0;
    updateConnectionStatus(true);

    // 🔥 Admin vừa connect → mark read ngay
    setTimeout(() => {
        markMessagesAsRead();
    }, 500);

    // Subscribe to conversation messages
    stompClient.subscribe(`/topic/conversation/${conversationId}`, (message) => {
        const data = JSON.parse(message.body);
        if (data.clientTempId) {
            updateTempMessage(data);
        } else {
            displayMessage(data); // tin của người khác
        }
        playNotificationSound();
    });

    // Subscribe to typing indicator
    stompClient.subscribe(`/topic/conversation/${conversationId}/typing`, (message) => {
        const data = JSON.parse(message.body);
        if (data.userId !== userId) {
            showTyping(data.isTyping);
        }
    });

    // Subscribe to read receipts
    stompClient.subscribe(`/topic/conversation/${conversationId}/read`, (message) => {
        const data = JSON.parse(message.body);
        handleMessageRead(data);
    });

    // Load existing messages
    loadMessages();
}

function handleMessageRead(data) {
    const messageId = data.messageId;

    const statusEl = document.getElementById(`msg-status-${messageId}`);
    if (!statusEl) return;

    statusEl.innerHTML = renderStatusIcon("READ");
    statusEl.dataset.status = "READ";
}

function updateTempMessage(message) {
    const el = document.querySelector(
        `[data-message-id="${message.clientTempId}"]`
    );

    if (!el) return;

    // đổi tempId → id thật
    el.dataset.messageId = message.id;

    const statusEl = el.querySelector('.message-status');
    if (statusEl) {
        statusEl.id = `msg-status-${message.id}`; // 🔥 QUAN TRỌNG
        statusEl.innerHTML = renderStatusIcon(message.status);
        statusEl.dataset.status = message.status;
    }
}

function onConnectionError(error) {
    console.error('WebSocket connection error:', error);
    isConnected = false;
    updateConnectionStatus(false);
    scheduleReconnect();
}

function scheduleReconnect() {
    if (reconnectAttempts < maxReconnectAttempts) {
        reconnectAttempts++;
        const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), 30000);
        console.log(`Reconnecting in ${delay}ms... (Attempt ${reconnectAttempts}/${maxReconnectAttempts})`);
        setTimeout(connect, delay);
    } else {
        showErrorNotification('Không thể kết nối. Vui lòng tải lại trang.');
    }
}

function updateConnectionStatus(isOnline) {
    const statusDot = document.getElementById('onlineStatus');
    const statusText = document.getElementById('statusText');
    
    if (isOnline) {
        statusDot.className = 'status-dot status-online';
        statusText.textContent = 'Đang hoạt động';
    } else {
        statusDot.className = 'status-dot status-offline';
        statusText.textContent = 'Mất kết nối...';
    }
}

/* ========== MESSAGE HANDLING ========== */

function sendMessage() {
    const input = document.getElementById('messageInput');
    const content = input.value.trim();
    
    if (!content && !selectedFile) {
        return;
    }

    if (!isConnected) {
        showErrorNotification('Không có kết nối. Đang thử kết nối lại...');
        connect();
        return;
    }

    // If file is selected, upload first
    if (selectedFile) {
        uploadFileAndSendMessage(content);
    } else {
        sendTextMessage(content);
    }

    input.value = '';
    input.style.height = 'auto';
    stopTyping();
}

function sendTextMessage(content) {
    const tempId = 'tmp_' + Date.now();

    displayMessage({
        id: tempId,
        senderId: userId,
        senderName: 'You',
        content,
        status: 'SENDING',
        createdAt: new Date()
    });

    fetch(`${contextPath}/api/chat/${conversationId}/send`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
                content,
                clientTempId: tempId
        })
    })
    .then(res => res.json())
    .then(realMsg => {
        replaceTempMessage(tempId, realMsg);
    })
    .catch(() => {
         const statusEl = document.getElementById(`msg-status-${tempId}`);
         if (statusEl) {
             statusEl.innerHTML = renderMessageStatusIcon('FAILED');
             statusEl.dataset.status = 'FAILED';
         }
     });

}

function uploadFileAndSendMessage(content) {
    const formData = new FormData();
    formData.append('file', selectedFile);
    formData.append('conversationId', conversationId);
    formData.append('userId', userId);
    if (content) {
        formData.append('content', content);
    }

    // Show loading indicator
    showLoadingMessage();

    fetch(`${contextPath}/api/chat/${conversationId}/send`, {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (!response.ok) throw new Error('Upload failed');
        return response.json();
    })
    .then(data => {
        clearFile();
        removeLoadingMessage();
    })
    .catch(error => {
        console.error('Upload error:', error);
        removeLoadingMessage();
        showErrorNotification('Không thể gửi file. Vui lòng thử lại.');
    });
}
function renderStatusIcon(status) {
    if (status === 'SENDING')
        return '<span class="status sending">⏳</span>';

    if (status === 'SENT')
        return '<span class="status sent">✔</span>';

    if (status === 'READ')
        return '<span class="status read">✔✔</span>';

    if (status === 'FAILED')
        return '<span class="status failed">❌</span>';

    return '';
}


function loadMessages() {
    fetch(`${contextPath}/api/chat/${conversationId}/messages`)
        .then(response => {
            if (!response.ok) throw new Error('Failed to load messages');
            return response.json();
        })
        .then(messages => {
            const container = document.getElementById('messagesContainer');
            // Keep welcome message
            const welcomeMsg = container.querySelector('.text-center');
            container.innerHTML = '';
            if (welcomeMsg) container.appendChild(welcomeMsg);
            
            messages.forEach(msg => displayMessage(msg, false));
            scrollToBottom();
        })
        .catch(error => {
            console.error('Error loading messages:', error);
            showErrorNotification('Không thể tải tin nhắn. Vui lòng thử lại.');
        });
}

function displayMessage(message, animate = true) {
    if (document.querySelector(`[data-message-id="${message.id}"]`)) {
            return; // ⛔ tránh render trùng
        }
    const container = document.getElementById('messagesContainer');
    const isOwn = message.senderId === userId;

    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${isOwn ? 'sent' : 'received'} ${animate ? 'animate-message' : ''}`;
    messageDiv.dataset.messageId = message.id;

    let avatarHtml = '';
    if (!isOwn) {
        const initial = message.senderName ? message.senderName.charAt(0).toUpperCase() : 'A';
        avatarHtml = `<div class="avatar">${initial}</div>`;
    }

    let fileContent = '';
    if (message.fileUrl) {
        const fullUrl = message.fileUrl.startsWith('http') ? message.fileUrl : `${contextPath}${message.fileUrl}`;
        
        if (message.type === 'IMAGE') {
            fileContent = `
                <img src="${fullUrl}" 
                     class="file-preview-image" 
                     onclick="openImageModal('${fullUrl}')"
                     alt="Image" 
                     loading="lazy">
            `;
        } else if (message.type === 'VIDEO') {
            fileContent = `
                <video src="${fullUrl}" 
                       controls 
                       class="file-preview-video"
                       preload="metadata">
                    Trình duyệt không hỗ trợ video.
                </video>
            `;
        } else if (message.type === 'FILE') {
            const fileName = message.fileName || 'File đính kèm';
            const fileSize = message.fileSize ? formatFileSize(message.fileSize) : '';
            fileContent = `
                <div class="file-attachment">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                    </svg>
                    <div class="flex-1">
                        <p class="text-sm font-medium">${escapeHtml(fileName)}</p>
                        ${fileSize ? `<p class="text-xs opacity-70">${fileSize}</p>` : ''}
                    </div>
                    <a href="${fullUrl}" download class="hover:opacity-80">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path>
                        </svg>
                    </a>
                </div>
            `;
        }
    }

    const status = message.status || 'SENT';

    messageDiv.innerHTML = `
        <div class="message-row">
            ${avatarHtml}
            <div class="message-wrapper">
                ${!isOwn && message.senderName ? `<span class="text-xs text-gray-500 mb-1 px-2">${escapeHtml(message.senderName)}</span>` : ''}
                <div class="message-bubble">
                    ${fileContent}
                    ${message.content ? `<div class="message-content">${escapeHtml(message.content)}</div>` : ''}
                </div>
                    <div class="message-time">
                        ${formatTime(message.createdAt)}
                        ${isOwn ? `
                            <span class="message-status"
                                  id="msg-status-${message.id}"
                                  data-status="${status}">
                                ${renderStatusIcon(status)}
                            </span>
                        ` : ''}
                    </div>
            </div>
        </div>
    `;

    container.appendChild(messageDiv);
    scrollToBottom();
}

/* ========== TYPING INDICATOR ========== */

function handleTyping() {
    if (!isConnected) return;

    stompClient.send('/app/chat.typing', {}, JSON.stringify({
        conversationId: conversationId,
        userId: userId,
        isTyping: true
    }));

    clearTimeout(typingTimeout);
    typingTimeout = setTimeout(stopTyping, 1500);
}

function stopTyping() {
    if (!isConnected) return;
    
    stompClient.send('/app/chat.typing', {}, JSON.stringify({
        conversationId: conversationId,
        userId: userId,
        isTyping: false
    }));
}

function showTyping(isTyping) {
    const indicator = document.getElementById('typingIndicator');
    indicator.classList.toggle('hidden', !isTyping);
    if (isTyping) scrollToBottom();
}

/* ========== FILE HANDLING ========== */

function triggerFileUpload() {
    document.getElementById('fileInput').click();
}

function handleFileSelect(event) {
    const file = event.target.files[0];
    if (!file) return;

    // Validate file size (max 10MB)
    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) {
        showErrorNotification('File quá lớn. Kích thước tối đa là 10MB.');
        event.target.value = '';
        return;
    }

    selectedFile = file;
    showFilePreview(file);
}

function showFilePreview(file) {
    const container = document.getElementById('filePreviewContainer');
    const previewImage = document.getElementById('filePreviewImage');
    const previewIcon = document.getElementById('filePreviewIcon');
    const fileName = document.getElementById('fileName');
    const fileSize = document.getElementById('fileSize');

    fileName.textContent = file.name;
    fileSize.textContent = formatFileSize(file.size);

    if (file.type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onload = (e) => {
            previewImage.src = e.target.result;
            previewImage.classList.remove('hidden');
            previewIcon.classList.add('hidden');
        };
        reader.readAsDataURL(file);
    } else {
        previewImage.classList.add('hidden');
        previewIcon.classList.remove('hidden');
    }

    container.classList.remove('hidden');
}

function clearFile() {
    selectedFile = null;
    document.getElementById('fileInput').value = '';
    document.getElementById('filePreviewContainer').classList.add('hidden');
    document.getElementById('filePreviewImage').src = '';
}

/* ========== UI UTILITIES ========== */

function autoResizeTextarea(textarea) {
    textarea.style.height = 'auto';
    textarea.style.height = Math.min(textarea.scrollHeight, 150) + 'px';
}

function scrollToBottom(smooth = true) {
    const container = document.getElementById('messagesContainer');
    const scrollOptions = smooth ? { behavior: 'smooth', block: 'end' } : { block: 'end' };
    
    setTimeout(() => {
        container.scrollTo({
            top: container.scrollHeight,
            ...scrollOptions
        });
    }, 100);
}

function formatTime(timestamp) {
    if (!timestamp) return '';
    
    const date = new Date(timestamp);
    const now = new Date();
    const diffInHours = (now - date) / (1000 * 60 * 60);

    if (diffInHours < 24) {
        return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
    } else if (diffInHours < 48) {
        return 'Hôm qua ' + date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
    } else {
        return date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' }) + ' ' + 
               date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
    }
}

function formatFileSize(bytes) {
    if (!bytes) return '';
    
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text || '';
    return div.innerHTML;
}

/* ========== IMAGE MODAL ========== */

function openImageModal(imageUrl) {
    const modal = document.getElementById('imageModal');
    const modalImage = document.getElementById('modalImage');
    
    modalImage.src = imageUrl;
    modal.classList.add('active');
    
    document.body.style.overflow = 'hidden';
}

function closeImageModal() {
    const modal = document.getElementById('imageModal');
    modal.classList.remove('active');
    document.body.style.overflow = 'auto';
}

/* ========== NOTIFICATIONS ========== */

function showErrorNotification(message) {
    // You can implement toast notifications here
    alert(message);
}

function playNotificationSound() {
    // Optional: play a subtle notification sound
    // const audio = new Audio('/path/to/notification.mp3');
    // audio.volume = 0.3;
    // audio.play().catch(e => console.log('Could not play sound'));
}

function showLoadingMessage() {
    const container = document.getElementById('messagesContainer');
    const loadingDiv = document.createElement('div');
    loadingDiv.id = 'loadingMessage';
    loadingDiv.className = 'message sent';
    loadingDiv.innerHTML = `
        <div class="message-wrapper">
            <div class="message-bubble flex items-center gap-2">
                <div class="spinner"></div>
                <span>Đang gửi...</span>
            </div>
        </div>
    `;
    container.appendChild(loadingDiv);
    scrollToBottom();
}

function removeLoadingMessage() {
    const loadingMsg = document.getElementById('loadingMessage');
    if (loadingMsg) loadingMsg.remove();
}

/* ========== ADDITIONAL FEATURES ========== */

function clearChat() {
    if (!confirm('Bạn có chắc muốn xóa tất cả tin nhắn?')) return;
    
    fetch(`${contextPath}/api/chat/${conversationId}/clear`, {
        method: 'DELETE'
    })
    .then(response => {
        if (response.ok) {
            const container = document.getElementById('messagesContainer');
            const welcomeMsg = container.querySelector('.text-center');
            container.innerHTML = '';
            if (welcomeMsg) container.appendChild(welcomeMsg);
        }
    })
    .catch(error => {
        console.error('Error clearing chat:', error);
        showErrorNotification('Không thể xóa tin nhắn.');
    });
}

function markMessagesAsRead() {
    if (!isConnected) return;
    
    fetch(`${contextPath}/api/chat/${conversationId}/read`, {
        method: 'POST'
    }).catch(error => console.error('Error marking messages as read:', error));
}

/* ========== EVENT HANDLERS ========== */

function handleKeyPress(event) {
    if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault();
        sendMessage();
    }
}

// Visibility change handler to mark messages as read
document.addEventListener('visibilitychange', () => {
    if (!document.hidden) {
        markMessagesAsRead();
    }
});

// Mark messages as read when scrolling to bottom
document.getElementById('messagesContainer')?.addEventListener('scroll', (e) => {
    const container = e.target;
    const isAtBottom = container.scrollHeight - container.scrollTop <= container.clientHeight + 100;
    
    if (isAtBottom) {
        markMessagesAsRead();
    }
});

// Close image modal with Escape key
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        closeImageModal();
    }
});

// Prevent modal from closing when clicking on image
document.getElementById('modalImage')?.addEventListener('click', (e) => {
    e.stopPropagation();
});

/* ========== INITIALIZATION ========== */

document.addEventListener('DOMContentLoaded', () => {
    // Focus on input
    document.getElementById('messageInput')?.focus();
    
    // Start connection
    connect();
    
    // Mark messages as read on load
    setTimeout(markMessagesAsRead, 1000);
});

// Cleanup on page unload
window.addEventListener('beforeunload', () => {
    if (stompClient && isConnected) {
        stompClient.disconnect();
    }
});