package com.frauddetection.model;

import com.frauddetection.entity.FraudRecord;

/** Spring event published after fraud detection — triggers async alert. */
public record AlertEvent(FraudRecord fraudRecord) {}
