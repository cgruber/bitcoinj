/**
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.bitcoin.wallet;

import com.google.bitcoin.core.BloomFilter;
import com.google.bitcoin.core.ECKey;
import org.bitcoinj.wallet.Protos;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * <p>A KeyChain is a class that stores a collection of keys for a {@link com.google.bitcoin.core.Wallet}. Key chains
 * are expected to be able to look up keys given a hash (i.e. address) or pubkey bytes, and provide keys on request
 * for a given purpose. They can inform event listeners about new keys being added.</p>
 *
 * <p>However it is important to understand what this interface does <i>not</i> provide. It cannot encrypt or decrypt
 * keys, for instance you need an implementor of {@link EncryptableKeyChain}. It cannot have keys imported into it,
 * that you to use a method of a specific key chain instance, such as {@link BasicKeyChain}. The reason for these
 * restrictions is to support key chains that may be handled by external hardware or software, or which are derived
 * deterministically from a seed (and thus the notion of importing a key is meaningless).</p>
 */
public interface KeyChain {
    /**
     * Locates a keypair from the keychain given the hash of the public key. This is needed when finding out which
     * key we need to use to redeem a transaction output.
     *
     * @return ECKey object or null if no such key was found.
     */
    public ECKey findKeyFromPubHash(byte[] pubkeyHash);

    /**
     * Locates a keypair from the keychain given the raw public key bytes.
     * @return ECKey or null if no such key was found.
     */
    public ECKey findKeyFromPubKey(byte[] pubkey);

    /** Returns true if the given key is in the chain. */
    public boolean hasKey(ECKey key);

    enum KeyPurpose {
        RECEIVE_FUNDS,
        CHANGE
    }

    /** Obtains a key intended for the given purpose. The chain may create a new key, derive one, or re-use an old one. */
    public ECKey getKey(KeyPurpose purpose);

    /** Returns a list of keys serialized to the bitcoinj protobuf format. */
    public List<Protos.Key> serializeToProtobuf();

    /** Adds a listener for events that are run when keys are added, on the user thread. */
    public void addEventListener(KeyChainEventListener listener);

    /** Adds a listener for events that are run when keys are added, on the given executor. */
    public void addEventListener(KeyChainEventListener listener, Executor executor);

    /** Removes a listener for events that are run when keys are added. */
    public boolean removeEventListener(KeyChainEventListener listener);

    /** Returns the number of keys this key chain manages. */
    public int numKeys();

    /**
     * Returns the number of elements this chain wishes to insert into the Bloom filter. The size passed to
     * {@link #getFilter(int, double, long)} should be at least this large.
     */
    public int numBloomFilterEntries();

    /**
     * <p>Gets a bloom filter that contains all of the public keys from this chain, and which will provide the given
     * false-positive rate if it has size elements. Keep in mind that you will get 2 elements in the bloom filter for
     * each key in the key chain, for the public key and the hash of the public key (address form). For this reason
     * size should be <i>at least</i> 2x the result of {@link #numKeys()}.</p>
     *
     * <p>This is used to generate a {@link BloomFilter} which can be {@link BloomFilter#merge(BloomFilter)}d with
     * another. It could also be used if you have a specific target for the filter's size.</p>
     *
     * <p>See the docs for {@link com.google.bitcoin.core.BloomFilter#BloomFilter(int, double, long)} for a brief
     * explanation of anonymity when using bloom filters, and for the meaning of these parameters.</p>
     */
    public BloomFilter getFilter(int size, double falsePositiveRate, long tweak);
}
