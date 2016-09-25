package com.greghaskins.spectrum;

import com.greghaskins.spectrum.Spectrum.Block;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

import java.util.Deque;
import java.util.LinkedList;

class TeardownBlock implements NotifyingBlock {

  private final Deque<NotifyingBlock> blocks;

  public TeardownBlock() {
    this.blocks = new LinkedList<>();
  }

  @Override
  public void run(final Description description, final RunNotifier notifier) {
    this.blocks.descendingIterator().forEachRemaining((block) -> block.run(description, notifier));
  }

  void addBlock(final Block block) {
    addBlock(NotifyingBlock.wrap(block));
  }

  void addBlock(final NotifyingBlock notifyingBlock) {
    this.blocks.add(notifyingBlock);
  }

}
