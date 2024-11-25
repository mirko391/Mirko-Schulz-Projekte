document.addEventListener('DOMContentLoaded', function() {
    var calendarEl = document.getElementById('calendar');
    var currentPersonId = [[${#authentication.principal.personid}]];

    var calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        locale: 'de',
        editable: true,
        events: '/api/todos',
        dateClick: function(info) {
            var title = prompt('Titel des Todos:');
            if (title) {
                fetch('/api/todos', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        titel: title,
                        start: info.dateStr,
                        ende: info.dateStr,
                        person: currentPersonId,
                        status: "Offen"
                    })
                })
                .then(response => response.ok ? response.json() : Promise.reject(response.statusText))
                .then(data => calendar.addEvent({ id: data.id, title: data.titel, start: data.start, end: data.ende }))
                .catch(error => console.error('Error:', error));
            }
        },
        eventClick: function(info) {
            if (confirm('Möchtest du dieses Todo löschen?')) {
                fetch('/api/todos/' + info.event.id, { method: 'DELETE' })
                .then(() => info.event.remove())
                .catch(error => console.error('Error:', error));
            }
        },
        eventDrop: function(info) {
            if (!confirm("Bist du sicher, dass du dieses Event verschieben möchtest?")) {
                info.revert();
                return;
            }

            const newStart = info.event.start.toISOString().split("T")[0];
            const newEnd = info.event.end ? info.event.end.toISOString().split("T")[0] : newStart;

            fetch('/api/todos/' + info.event.id, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ start: newStart, ende: newEnd })
            })
            .then(response => response.ok ? response : Promise.reject(response.statusText))
            .catch(error => {
                console.error('Error:', error);
                info.revert();
            });
        }
    });

    calendar.render();

    document.getElementById('todoForm').addEventListener('submit', function(event) {
        event.preventDefault();
        var title = document.getElementById('todoTitel').value;
        var start = document.getElementById('todoStart').value;
        var end = document.getElementById('todoEnde').value;
        var status = document.getElementById('todoStatus').value;

        fetch('/api/todos', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                titel: title,
                start: start,
                ende: end,
                person: currentPersonId,
                status: status
            })
        })
        .then(response => response.ok ? response.json() : Promise.reject(response.statusText))
        .then(data => {
            calendar.addEvent({ id: data.todoid, title: data.titel, start: data.start, end: data.ende });
            document.getElementById('todoForm').reset();
        })
        .catch(error => console.error('Error:', error));
    });
});

