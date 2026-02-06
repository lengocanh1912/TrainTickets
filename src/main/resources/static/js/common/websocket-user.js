if (window.APP_CONTEXT?.role === 'USER') {

    const socket = new SockJS('/trainticket/ws-chat');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {

        const userId = window.APP_CONTEXT.userId;

        stompClient.subscribe(
            `/queue/user/${userId}/notification`,
            () => increaseBadge()
        );
    });

    function increaseBadge() {
        const badge = document.getElementById('support-unread-badge');
        if (!badge) return;

        let count = parseInt(badge.innerText || '0');
        count++;

        badge.style.display = 'inline-block';
        badge.innerText = count > 9 ? '9+' : count;
    }
}
