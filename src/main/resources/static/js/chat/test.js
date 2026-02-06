<script th:inline="javascript">
    /*<![CDATA[*/
    const currentUserId =[[${#authentication.principal.user.id}]];
    const adminId = /*[[${adminId}]]*/ null;
    const contextPath = /*[[@{/}]]*/ '/';

    let currentCustomerId = null;
    let presenceSubscription = null;
    let stompClient = null;
    let currentConversationId = null;
    let conversations = [];
    let selectedFile = null;
    let typingTimeout = null;
    let messageSubscription = null;
    let typingSubscription = null;
    let totalUnread = 0;

    console.log('=== ADMIN CHAT INITIALIZED ===');
    console.log('AdminId:', adminId);
    console.log('Context Path:', contextPath);

    // Connect WebSocket
    function connect() {
        console.log('>>> Connecting to WebSocket...');
        const socket = new SockJS(contextPath + 'ws-chat');
        stompClient = Stomp.over(socket);

        stompClient.debug = str => console.log('STOMP:', str);

        stompClient.connect({}, function(frame) {
            console.log('✅ Admin WebSocket Connected!');
            // 🔔 notify khi conversation đã claim
            stompClient.subscribe(
                '/queue/admin/' + adminId + '/notification',
                onAdminNotify
            );
            // 🔔 notify khi conversation WAITING
            stompClient.subscribe(
                '/topic/admin/notification',
                onAdminNotify
            );
            // 🔔 new conversation assigned
            stompClient.subscribe(
                '/queue/admin/' + adminId + '/new',
                () => loadConversations()
            );

            loadConversations();
        }, function(error) {
            console.error('❌ WebSocket connection error:', error);
        });
    }

    function onAdminNotify(msg) {
        const message = JSON.parse(msg.body);
        console.log('🔔 ADMIN NOTIFY', message);

        // ❌ Nếu đang mở conversation này → bỏ qua notify
        if (message.conversationId === currentConversationId) return;

        const conv = conversations.find(c => c.id === message.conversationId);

        if (conv) {
            // 🔥 update nội dung
            conv.lastMessageContent = message.content || '[Attachment]';
            conv.lastMessageTime = message.createdAt;
            conv.unreadCount = (conv.unreadCount || 0) + 1;

            // 🔥 ĐẨY CONVERSATION LÊN ĐẦU LIST
            conversations = [
                conv,
                ...conversations.filter(c => c.id !== conv.id)
            ];

            displayConversations();
            playNotifySound();
        }

    }

    function loadConversations() {
        fetch(contextPath + 'api/admin/conversations')
            .then(res => res.json())
            .then(data => {
                conversations = data;
                displayConversations();
            })
            .catch(err => console.error(err));
    }


    function displayConversations() {
    const container = document.getElementById('conversationList');

    document.getElementById('waitingCount').textContent = conversations.length;

    if (conversations.length === 0) {
        container.innerHTML =
            '<div class="p-4 text-center text-gray-500 text-sm">No conversations</div>';
        return;
    }

    container.innerHTML = conversations.map(conv => {
        const isMe = currentUserId &&
            conv.lastMessageSenderId === currentUserId;

        const preview = conv.lastMessageContent
            ? `${isMe ? 'Bạn' : conv.lastMessageSenderName}: ${conv.lastMessageContent}`
            : '';

        return `
        <div class="conversation-item p-4 border-b cursor-pointer hover:bg-gray-50
            ${conv.id === currentConversationId ? 'bg-blue-50' : ''}"
            data-conversation-id="${conv.id}"
            onclick="selectConversation(${conv.id})">

            <div class="flex items-start gap-3">
                <div class="w-10 h-10 bg-purple-600 rounded-full flex items-center justify-center text-white">
                    ${conv.userName?.charAt(0) || 'U'}
                </div>

                <div class="flex-1 min-w-0">
                    <div class="flex justify-between">
                        <h3 class="font-semibold truncate">${conv.userName}</h3>
                        <span class="text-xs text-gray-500">
                            ${formatTime(conv.lastMessageTime)}
                        </span>
                    </div>

                    <p class="text-sm truncate text-gray-600">
                        ${preview}
                    </p>
                </div>

                <div class="unread-container ml-2">
                    ${conv.unreadCount > 0
                        ? `<span class="bg-red-500 text-white text-xs px-2 py-1 rounded-full">
                               ${conv.unreadCount > 9 ? '9+' : conv.unreadCount}
                           </span>`
                        : ''
                    }
                </div>
            </div>
        </div>
        `;
    }).join('');

    renderTotalUnread();
}



    function renderStatusBadge(status) {
        if (status === 'WAITING')
            return `<span class="text-xs bg-yellow-100 text-yellow-700 px-2 py-0.5 rounded">Waiting</span>`;
        if (status === 'CLAIMED')
            return `<span class="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded">Processing</span>`;
        if (status === 'CLOSED')
            return `<span class="text-xs bg-gray-200 text-gray-600 px-2 py-0.5 rounded">Closed</span>`;
        return '';
    }

    function reloadCustomerInfo() {
            fetch(`${contextPath}/api/admin/conversation/${conversationId}/info`)
            .then(res => res.json())
            .then(data => {
                updateOnlineStatus(data.online);
            });
    }
    function updateOnlineStatus(isOnline) {
        const el = document.getElementById("userStatus");
        if (!el) return;

        if (isOnline) {
            el.textContent = "🟢 Online";
            el.className = "text-green-600";
        } else {
            el.textContent = "⚪ Offline";
            el.className = "text-gray-400";
        }
    }


    async function selectConversation(conversationId) {
    console.log('>>> Selecting conversationId:', conversationId);
    currentConversationId = conversationId;

    const conversation = conversations.find(c => c.id === conversationId);
    // 🔥 LẤY USER ID TỪ CONVERSATION
    currentCustomerId = conversation.userId;

    // 🔥 UNSUBSCRIBE CŨ
    if (presenceSubscription) {
        presenceSubscription.unsubscribe();
        presenceSubscription = null;
    }

    // 🔥 SUBSCRIBE PRESENCE MỚI
     stompClient.subscribe("/topic/presence", function (message) {
         const userId = JSON.parse(message.body);

         if (userId === currentCustomerId) {
             reloadCustomerInfo();
         }
     });


    if (!conversation) return;

    // ❌ Closed thì không cho chat
    if (conversation.status === 'CLOSED') {
        alert('Conversation is closed');
        return;
    }

    // 🔥 1. CLAIM NẾU ĐANG WAITING
    if (conversation.status === 'WAITING') {
        await claimConversation(conversationId);

        // 🔥 reload lại state từ server
        await loadConversations();

        // 🔥 lấy lại object mới
        const updated = conversations.find(c => c.id === conversationId);
        if (!updated || updated.status !== 'OPEN') {
            alert('Conversation cannot be claimed');
            return;
        }
    }
    // ❌ Admin khác đang xử lý
    if (conversation.adminId && conversation.adminId !== adminId) {
        alert('Conversation is being handled by another admin');
        return;
    }

    // 🔥 2. UI
    const emptyState = document.getElementById('emptyState');
    const chatContent = document.getElementById('chatContent');
    const customerInfo = document.getElementById('customerInfo');

    if (emptyState) emptyState.classList.add('hidden');
    if (chatContent) chatContent.classList.remove('hidden');
    if (customerInfo) customerInfo.classList.remove('hidden');

    // 🔥 3. UNSUBSCRIBE CŨ
    if (messageSubscription) messageSubscription.unsubscribe();
    if (typingSubscription) typingSubscription.unsubscribe();

    // 🔥 4. SUBSCRIBE MỚI
    messageSubscription = stompClient.subscribe(
        '/topic/conversation/' + conversationId,
        msg => {
            displayMessage(JSON.parse(msg.body));
            scrollToBottom();
        }
    );

    typingSubscription = stompClient.subscribe(
        '/topic/conversation/' + conversationId + '/typing',
        data => {
            const indicator = JSON.parse(data.body);
            if (indicator.userId !== adminId) {
                showTypingIndicator(indicator.isTyping);
            }
        }
    );

    loadMessages(conversationId);
    loadCustomerInfo(conversationId);

    // 🔥 ĐÁNH DẤU READ TẠI ĐÂY
        await markAsRead(conversationId);

        const conv = conversations.find(c => c.id === conversationId);
        if (conv) {
            totalUnread -= conv.unreadCount || 0;
            conv.unreadCount = 0;
            renderTotalUnread();
            displayConversations();
        }
}

    function claimConversation(conversationId) {
        return fetch(contextPath + `api/admin/conversation/${conversationId}/claim`, {
            method: 'POST'
        }).then(res => {
            if (!res.ok) throw new Error('Claim failed');
            return res.json();
        });
    }


    function loadMessages(conversationId) {
        console.log('>>> Loading messages for conversation:', conversationId);
        fetch(contextPath + `api/chat/${conversationId}/messages`)
            .then(response => {
                console.log('Response status:', response.status);
                return response.json();
            })
            .then(messages => {
                console.log('📥 Loaded ' + messages.length + ' messages:', messages);
                const container = document.getElementById('messagesContainer');
                container.innerHTML = '';
                messages.forEach(msg => displayMessage(msg));
                scrollToBottom();
            })
            .catch(error => {
                console.error('❌ Error loading messages:', error);
            });
    }

    function displayMessage(message) {
        console.log('>>> Displaying message:', message);
        const container = document.getElementById('messagesContainer');
        const isOwn = message.senderId === adminId;

        console.log('isOwn:', isOwn, '| senderId:', message.senderId, '| adminId:', adminId);

        const messageDiv = document.createElement('div');
        messageDiv.className = `flex ${isOwn ? 'justify-end' : 'justify-start'} mb-4`;
        messageDiv.dataset.messageId = message.id;

        let fileContent = '';
        if (message.fileUrl) {
            if (message.type === 'IMAGE') {
                fileContent = `<img src="${contextPath}${message.fileUrl}" class="rounded-lg max-w-xs mb-2 cursor-pointer" onclick="openImage('${contextPath}${message.fileUrl}')">`;
            } else if (message.type === 'VIDEO') {
                fileContent = `<video src="${contextPath}${message.fileUrl}" controls class="rounded-lg max-w-xs mb-2"></video>`;
            } else {
                fileContent = `
                    <a href="${contextPath}${message.fileUrl}" download class="bg-gray-100 p-3 rounded-lg mb-2 block hover:bg-gray-200">
                        <p class="text-sm font-medium">${message.fileName}</p>
                        <p class="text-xs text-gray-500">${formatFileSize(message.fileSize)}</p>
                    </a>
                `;
            }
        }

        messageDiv.innerHTML = `
            <div class="max-w-md">
                ${!isOwn ? `<p class="text-xs text-gray-500 mb-1 px-1">${escapeHtml(message.senderName)}</p>` : ''}
                <div class="${isOwn ? 'bg-blue-600 text-white' : 'bg-white border'} rounded-2xl px-4 py-3">
                    ${fileContent}
                    ${message.content ? `<p class="break-words whitespace-pre-wrap">${escapeHtml(message.content)}</p>` : ''}
                    <div class="flex items-center gap-2 mt-1 text-xs ${isOwn ? 'text-blue-100' : 'text-gray-500'}">
                        <span>${formatTime(message.createdAt)}</span>
                    </div>
                </div>
            </div>
        `;



        container.appendChild(messageDiv);
    }

    function sendMessage() {
        const conv = conversations.find(c => c.id === currentConversationId);
        if (!conv || conv.status !== 'OPEN') {
            alert('You must claim the conversation before sending messages');
            return;
        }

        if (!currentConversationId) {
            console.log('⚠️ No conversation selected');
            return;
        }

        const input = document.getElementById('messageInput');
        const content = input.value.trim();

        console.log('>>> Sending message:', content);

        if (!content && !selectedFile) {
            console.log('⚠️ No content to send');
            return;
        }

        if (selectedFile) {
            console.log('>>> Sending with file...');
            const formData = new FormData();
            formData.append('content', content);
            formData.append('file', selectedFile);

            fetch(contextPath + `api/chat/${currentConversationId}/send`, {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(message => {
                console.log('✅ File message sent:', message);

                // 🔥 UPDATE LIST CONVERSATION REALTIME (FILE)
                const conv = conversations.find(c => c.id === currentConversationId);
                if (conv) {
                    conv.lastMessageContent = '[Attachment]';
                    conv.lastMessageSenderId = adminId;
                    conv.lastMessageSenderName = 'Bạn';
                    conv.lastMessageTime = message.createdAt || new Date().toISOString();

                    conversations = [
                        conv,
                        ...conversations.filter(c => c.id !== conv.id)
                    ];

                    displayConversations();
                }

                input.value = '';
                clearFile();
                scrollToBottom();
            })

            .catch(error => {
                console.error('❌ Error sending file message:', error);
            });
        } else {
            console.log('>>> Sending via WebSocket...');

            if (!stompClient || !stompClient.connected) {
                console.error('❌ WebSocket not connected!');
                return;
            }

            stompClient.send("/app/chat.send", {}, JSON.stringify({
                conversationId: currentConversationId,
                userId: adminId,
                content: content
            }));

            // 🔥 UPDATE LIST REALTIME (admin gửi)
                const conv = conversations.find(c => c.id === currentConversationId);
                if (conv) {
                    conv.lastMessageContent = content;
                    conv.lastMessageSenderId = adminId;
                    conv.lastMessageSenderName = 'Bạn';
                    conv.lastMessageTime = new Date().toISOString();

                    // đẩy lên đầu
                    conversations = [
                        conv,
                        ...conversations.filter(c => c.id !== conv.id)
                    ];

                    displayConversations();
                }


            console.log('✅ Message sent via WebSocket');
            input.value = '';
        }
    }

    function handleTyping() {
        if (!currentConversationId) return;

        if (typingTimeout) clearTimeout(typingTimeout);

        stompClient.send("/app/chat.typing", {}, JSON.stringify({
            conversationId: currentConversationId,
            userId: adminId,
            isTyping: true
        }));

        typingTimeout = setTimeout(() => {
            stompClient.send("/app/chat.typing", {}, JSON.stringify({
                conversationId: currentConversationId,
                userId: adminId,
                isTyping: false
            }));
        }, 1000);
    }

    function showTypingIndicator(show) {
        document.getElementById('typingIndicator').classList.toggle('hidden', !show);
        if (show) scrollToBottom();
    }

    function insertQuickReply(text) {
        document.getElementById('messageInput').value = text;
        document.getElementById('messageInput').focus();
    }

    function markAsRead(conversationId) {
        fetch(contextPath + `api/chat/${conversationId}/read`, {
            method: 'POST'
        });
    }

    function closeConversation() {
        if (!currentConversationId) return;

        if (confirm('Are you sure you want to close this conversation?')) {
            fetch(contextPath + `api/admin/conversation/${currentConversationId}/close`, {
                method: 'POST'
            })
            .then(() => {
                alert('Conversation closed successfully');
                currentConversationId = null;
                document.getElementById('chatContent').classList.add('hidden');
                document.getElementById('customerInfo').classList.add('hidden');
                document.getElementById('emptyState').classList.remove('hidden');
                loadConversations();
            })
            .catch(error => {
                console.error('Error closing conversation:', error);
            });
        }
    }
    function updateUnreadBadge(conversationId, unreadCount) {

        // ❌ đang mở thì không hiện badge
        if (conversationId === currentConversationId) return;

        const item = document.querySelector(
            `.conversation-item[data-conversation-id="${conversationId}"]`
        );
        if (!item) return;

        const container = item.querySelector('.unread-container');
        if (!container) return;

        let badge = container.querySelector('.unread-badge');

        if (unreadCount > 0) {
            if (!badge) {
                badge = document.createElement('span');
                badge.className =
                    'unread-badge bg-red-500 text-white text-xs w-5 h-5 rounded-full flex items-center justify-center animate-pulse';
                container.appendChild(badge);
            }
            badge.textContent = unreadCount > 9 ? '9+' : unreadCount;


            playNotifySound();
        } else {
            if (badge) badge.remove();
        }
    }

    function incrementTotalUnread() {
        totalUnread++;
        renderTotalUnread();
    }

    function decrementTotalUnread() {
        totalUnread = Math.max(0, totalUnread - 1);
        renderTotalUnread();
    }

    function renderTotalUnread() {
        const badge = document.getElementById('totalUnreadBadge');
        if (!badge) return;

        if (totalUnread > 0) {
            badge.textContent = totalUnread > 9 ? '9+' : totalUnread;
            badge.classList.remove('hidden');
        } else {
            badge.classList.add('hidden');
        }
    }


    function filterConversations() {
        const query = document.getElementById('searchConversations').value.toLowerCase();
        document.querySelectorAll('.conversation-item').forEach(item => {
            const text = item.textContent.toLowerCase();
            item.style.display = text.includes(query) ? 'block' : 'none';
        });
    }

    function handleFileSelect(event) {
        const file = event.target.files[0];
        if (file) {
            if (file.size > 10 * 1024 * 1024) {
                alert('File size must be less than 10MB');
                return;
            }

            selectedFile = file;
            document.getElementById('fileName').textContent = file.name;
            document.getElementById('fileSize').textContent = formatFileSize(file.size);

            if (file.type.startsWith('image/')) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    const img = document.getElementById('filePreviewImage');
                    img.src = e.target.result;
                    img.classList.remove('hidden');
                };
                reader.readAsDataURL(file);
            }

            document.getElementById('filePreview').classList.remove('hidden');
        }
    }

    function toggleCustomerModal() {
        document.getElementById('customerModal')
            .classList.toggle('hidden');
    }

    function triggerFileUpload() {
        document.getElementById('fileInput').click();
    }

    function clearFile() {
        selectedFile = null;
        document.getElementById('fileInput').value = '';
        document.getElementById('filePreview').classList.add('hidden');
        document.getElementById('filePreviewImage').classList.add('hidden');
    }

    function handleKeyPress(event) {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            sendMessage();
        }
    }

    function scrollToBottom() {
        const container = document.getElementById('messagesContainer');
        container.scrollTop = container.scrollHeight;
    }

    function formatTime(timestamp) {
        if (!timestamp) return '';

        const date = new Date(timestamp);
        const now = new Date();
        const diff = now - date;

        if (diff < 60000) return 'Just now';
        if (diff < 3600000) return Math.floor(diff / 60000) + 'm ago';
        if (diff < 86400000) return Math.floor(diff / 3600000) + 'h ago';

        return date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' });
    }

    function formatFileSize(bytes) {
        if (!bytes) return '';
        return bytes < 1024 * 1024
            ? `${(bytes / 1024).toFixed(1)} KB`
            : `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
    }

    function escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function openImage(url) {
        window.open(url, '_blank');
    }

    function loadCustomerInfo(conversationId) {
        fetch(contextPath + `api/admin/conversation/${conversationId}/info`)
            .then(res => res.json())
            .then(info => {
                updateHeaderCustomer(info);

                document.getElementById('customerName').textContent = info.userName;
                document.getElementById('customerInitial').textContent =
                    info.userName?.charAt(0) || 'U';
                document.getElementById('customerEmail').textContent = info.email || '—';
                document.getElementById('customerPhone').textContent = info.phone || '—';
                document.getElementById('customerStatus').textContent =
                    info.online ? '🟢 Online' : '⚪ Offline';
            });
    }


    function renderCustomerInfo(info) {
        document.getElementById('cusName').textContent = info.userName;
        document.getElementById('cusEmail').textContent = info.email || '—';
        document.getElementById('cusPhone').textContent = info.phone || '—';

        const status = document.getElementById('cusStatus');
        if (info.online) {
            status.textContent = 'Online';
            status.className = 'text-green-600';
        } else {
            status.textContent = 'Offline';
            status.className = 'text-gray-400';
        }
    }

    function updateHeaderCustomer(info) {
        document.getElementById('userName').textContent = info.userName;
        document.getElementById('userInitial').textContent =
            info.userName?.charAt(0) || 'U';

        const status = document.getElementById('userStatus');
        if (info.Online) {
            status.textContent = 'Online';
            status.className = 'text-green-600';
        } else {
            status.textContent = 'Offline';
            status.className = 'text-gray-400';
        }
    }

    function closeChatWindow() {
        document.getElementById('chatContent').classList.add('hidden');
        document.getElementById('emptyState').classList.remove('hidden');
        if (presenceSubscription) {
            presenceSubscription.unsubscribe();
            presenceSubscription = null;
        }
        currentCustomerId = null;

        // ❌ KHÔNG markAsRead
        // ❌ KHÔNG reset currentConversationId
    }

    function playNotifySound() {
        const audio = document.getElementById('notifySound');
        if (audio) {
            audio.currentTime = 0;
            audio.play().catch(() => {});
        }
    }
    // 🔓 Unlock audio on first user interaction (Chrome autoplay policy)
    document.addEventListener('click', () => {
        const audio = document.getElementById('notifySound');
        if (audio) {
            audio.play().then(() => {
                audio.pause();
                audio.currentTime = 0;
            }).catch(() => {});
        }
    }, { once: true });

    // Initialize
    connect();
    /*]]>*/
</script>
