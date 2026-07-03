package com.pulseboard.checkin.config;

import com.pulseboard.checkin.model.Attendee;
import com.pulseboard.checkin.model.Session;
import com.pulseboard.checkin.repository.AttendeeRepository;
import com.pulseboard.checkin.repository.SessionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(SessionRepository sessionRepository, AttendeeRepository attendeeRepository) {
        return args -> {
            if (sessionRepository.count() > 0 || attendeeRepository.count() > 0) {
                return;
            }

            // Capacities are kept small (rather than a real conference's hundreds)
            // so the seeded data already demonstrates every capacity state —
            // normal, nearly-full, and full — without having to check in dozens
            // of people by hand first.
            Session keynote = sessionRepository.save(Session.builder().name("Opening Keynote").capacity(10).build());
            Session scaling = sessionRepository.save(Session.builder().name("Scaling Real-Time Systems").capacity(5).build());
            Session design = sessionRepository.save(Session.builder().name("Design Systems at Scale").capacity(8).build());
            Session security = sessionRepository.save(Session.builder().name("Workshop: API Security").capacity(6).build());

            List<Attendee> attendees = new ArrayList<>();
            int ticketSeq = 10001;
            int minutesAgo = 90;

            // Opening Keynote — 6/10 checked in (normal).
            attendees.add(seedAttendee("Ava Whitfield", "ava.whitfield@lumen.io", ticketSeq++, keynote, true, true, minutesAgo -= 3));
            attendees.add(seedAttendee("Ethan Blackwood", "ethan.b@ironclad.dev", ticketSeq++, keynote, true, true, minutesAgo -= 4));
            attendees.add(seedAttendee("Nora Kimura", "nora.kimura@haven.io", ticketSeq++, keynote, false, true, minutesAgo -= 5));
            attendees.add(seedAttendee("Hannah Osei", "hannah.osei@brightline.io", ticketSeq++, keynote, false, true, minutesAgo -= 2));
            attendees.add(seedAttendee("Mei Lin", "mei.lin@northstar.dev", ticketSeq++, keynote, false, true, minutesAgo -= 6));
            attendees.add(seedAttendee("Isla Fraser", "isla.fraser@quantalabs.com", ticketSeq++, keynote, false, true, minutesAgo -= 3));
            attendees.add(seedAttendee("Grace Odongo", "grace.odongo@baobab.tech", ticketSeq++, keynote, false, false, null));
            attendees.add(seedAttendee("Ryan Castillo", "ryan.castillo@fernwood.dev", ticketSeq++, keynote, false, false, null));
            attendees.add(seedAttendee("Owen Bennett", "owen.bennett@driftlab.com", ticketSeq++, keynote, false, false, null));
            attendees.add(seedAttendee("Noah Whitmore", "noah.whitmore@candor.ai", ticketSeq++, keynote, false, false, null));

            // Scaling Real-Time Systems — 5/5 checked in (at capacity).
            attendees.add(seedAttendee("Marcus Chen", "marcus.chen@northbridge.com", ticketSeq++, scaling, false, true, minutesAgo -= 4));
            attendees.add(seedAttendee("Liam O'Rourke", "liam.orourke@driftlab.com", ticketSeq++, scaling, false, true, minutesAgo -= 5));
            attendees.add(seedAttendee("Tobias Lindgren", "tobias.l@nordwave.se", ticketSeq++, scaling, false, true, minutesAgo -= 3));
            attendees.add(seedAttendee("Zara Ahmed", "zara.ahmed@parallax.io", ticketSeq++, scaling, false, true, minutesAgo -= 2));
            attendees.add(seedAttendee("Caleb Nakamura", "caleb.nakamura@ironclad.dev", ticketSeq++, scaling, false, true, minutesAgo -= 4));

            // Design Systems at Scale — 7/8 checked in (nearly full).
            attendees.add(seedAttendee("Priya Natarajan", "priya.n@fernwood.dev", ticketSeq++, design, true, true, minutesAgo -= 6));
            attendees.add(seedAttendee("Aaliyah Johnson", "aaliyah.johnson@basalt.co", ticketSeq++, design, true, true, minutesAgo -= 3));
            attendees.add(seedAttendee("Sofia Reyes", "sofia.reyes@candor.ai", ticketSeq++, design, false, true, minutesAgo -= 5));
            attendees.add(seedAttendee("Felix Bauer", "felix.bauer@sturmwerk.de", ticketSeq++, design, false, true, minutesAgo -= 2));
            attendees.add(seedAttendee("Ines Moreau", "ines.moreau@parlance.io", ticketSeq++, design, false, true, minutesAgo -= 4));
            attendees.add(seedAttendee("Dmitri Volkov", "dmitri.volkov@nordwave.se", ticketSeq++, design, false, true, minutesAgo -= 1));
            attendees.add(seedAttendee("Petra Novak", "petra.novak@quantalabs.com", ticketSeq++, design, false, true, minutesAgo -= 3));
            attendees.add(seedAttendee("Lucas Ferreira", "lucas.ferreira@baobab.tech", ticketSeq++, design, false, false, null));

            // Workshop: API Security — 3/6 checked in (normal).
            attendees.add(seedAttendee("Sam O'Brien", "sam.obrien@basalt.co", ticketSeq++, security, false, true, minutesAgo -= 2));
            attendees.add(seedAttendee("Yuki Tanaka", "yuki.tanaka@northstar.dev", ticketSeq++, security, false, true, minutesAgo -= 4));
            attendees.add(seedAttendee("Aiden Murphy", "aiden.murphy@brightline.io", ticketSeq++, security, false, true, minutesAgo -= 1));
            attendees.add(seedAttendee("Diego Alvarez", "diego.alvarez@basalt.co", ticketSeq++, security, false, false, null));
            attendees.add(seedAttendee("Chloe Martin", "chloe.martin@parallax.io", ticketSeq++, security, false, false, null));
            attendees.add(seedAttendee("Ravi Shankar", "ravi.shankar@quantalabs.com", ticketSeq++, security, false, false, null));

            attendeeRepository.saveAll(attendees);
        };
    }

    private static Attendee seedAttendee(
            String fullName,
            String email,
            int ticketSequence,
            Session session,
            boolean vip,
            boolean checkedIn,
            Integer minutesAgo
    ) {
        return Attendee.builder()
                .fullName(fullName)
                .email(email)
                .ticketId("TCK-" + ticketSequence)
                .session(session)
                .vip(vip)
                .checkedIn(checkedIn)
                .checkInTime(checkedIn ? LocalDateTime.now().minusMinutes(minutesAgo) : null)
                .build();
    }
}
