package Blockchain;

import java.util.ArrayList;
import java.util.List;

public class Blockchain {

    private List<Block> chain;
    private final int difficulty;

    public Blockchain(int difficulty) {
        this.chain = new ArrayList<>();
        this.difficulty = difficulty;
        // Genesis block (blok pertama) harus ditambahkan saat blockchain dibuat
        chain.add(createGenesisBlock());
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
        chain.add(newBlock);
    }

    public void addBlockFromLocalChain(Localchain localChain) {
        List<Block> blockFromLocalchain = new ArrayList<>(localChain.getChain());

        // Ambil hash dari blok terakhir dalam blockchain utama
        String previousHash = getLatestBlock().getHash();

        // Update previousHash dari Genesis Block di Localchain
        Block genesisBlock = blockFromLocalchain.get(0);
        genesisBlock.setPreviousHash(previousHash);

        // calculate hash semua blok di localchain agar tetap valid
        for (Block block : blockFromLocalchain) {
            block.recalculateHash();
        }

        for (Block block : blockFromLocalchain) {
            addBlock(block);
        }
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
        sb.append("==== BLOCKCHAIN ====\n");

        for (Block block : chain) {
            sb.append("Prev Hash: ").append(block.getPreviousHash()).append("\n")
                    .append("Hash    : ").append(block.getHash()).append("\n")
                    .append("Mined By: ").append(block.getMinedBy() != null ? block.getMinedBy().toString() : "Unknown").append("\n")
                    .append("-----------------------\n");
        }

        return sb.toString();
    }

}
