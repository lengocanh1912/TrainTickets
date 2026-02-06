if (window.APP_CONTEXT?.role === 'ADMIN') {

    const socket = new SockJS('/trainticket/ws-chat');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {

        const adminId = window.APP_CONTEXT.userId;

        stompClient.subscribe(
            `/queue/admin/${adminId}/notification`,
            () => increaseAdminBadge()
        );

        stompClient.subscribe(
            `/topic/admin/notification`,
            () => increaseAdminBadge()
        );
    });

    function increaseAdminBadge() {
        const badge = document.getElementById('admin-support-badge');
        if (!badge) return;

        let count = parseInt(badge.innerText || '0');
        count++;

        badge.style.display = 'inline-block';
        badge.innerText = count > 9 ? '9+' : count;
    }
}
