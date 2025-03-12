package Blockchain;

import core.SimScenario;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Localchain {

    private List<Block> chain;
    private int difficulty;
    private String name;
    private String hash;

    public Localchain(int difficulty) {
        this.chain = new ArrayList<>();
        this.difficulty = difficulty;
        // Genesis block (blok pertama) harus ditambahkan saat blockchain dibuat
        chain.add(createGenesisBlock());
    }

//    copy constructor
    public Localchain(Localchain other) {
        this.chain = other.chain;
        this.difficulty = other.difficulty;
        this.name = other.name;
        this.hash = other.hash;
    }
    
    
    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String calculateHash() {
         
         String totalHash = "";
        for (int i = 0; i < chain.size(); i++) {
            String hash = chain.get(i).getHash();
            totalHash = totalHash+" + " +hash;
        }
        StringBuilder data = new StringBuilder(totalHash);
     
        return applySHA256(data.toString());
    }
    private String applySHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    private Block createGenesisBlock() {
        List<Transaction> list = new ArrayList<>();
        list.add(new Transaction("Bellen", "Maria", 10, System.currentTimeMillis(), 0.5));
        return new Block("0", list, System.currentTimeMillis());
    }

    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    public void addBlock(Block newBlock) {
//        newBlock.mineBlock(difficulty);
        chain.add(newBlock);
    }

    public int chainSize() {
        return chain.size();
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public List<Block> getChain() {
        return chain;
    }

    public void setChain(List<Block> chain) {
        this.chain = chain;
    }
    
    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);

            // Periksa apakah hash saat ini masih valid
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                return false;
            }

            // Periksa apakah hash sebelumnya cocok dengan hash blok sebelumnya
            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                return false;
            }
        }
        return true;
    }

    public void printBlockchain() {
        for (Block block : chain) {
//            System.out.println("Index       : " + block.getIndex());
//            System.out.println("Timestamp   : " + block.getTimestamp());
//            System.out.println("Data        : " + block.getData());
            System.out.println("Hash        : " + block.getHash());
            System.out.println("Prev Hash   : " + block.getPreviousHash());
            System.out.println("-------------------------------");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("================================ LOCALCHAIN ================================\n");
        sb.append("Blockchain Title  : Localchain ").append(name).append("\n");
        sb.append("Difficulty Level  : ").append(difficulty).append("\n\n");
        for (Block block : chain) {
            sb.append(block.toString()).append("\n");
        }
        sb.append("============================================================================\n");
        return sb.toString();
    }

}
