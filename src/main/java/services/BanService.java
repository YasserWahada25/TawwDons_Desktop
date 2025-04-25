package services;

import models.BanRecord;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BanService {

    private final List<BanRecord> bans = new ArrayList<>();

    public void ban(String utilisateur, String mot) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        bans.add(new BanRecord(utilisateur, mot, date));
    }

    public List<BanRecord> getAllBans() {
        return new ArrayList<>(bans);
    }
}
