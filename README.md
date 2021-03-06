# Text_file_compressor


## What is Huffman coding?
Huffman coding is an entropy encoding algorithm used for lossless data compression. 
The term refers to the use of a variable-length code table for encoding a source symbol (such as a character in a file)
where the variable-length code table has been derived in a particular way based on the estimated probability of occurrence for each possible value of the source symbol."

## Variable Length entropy coding systems
The idea was to use variable length encoding than using fixed length encoding. The fact that some characters occurs more frequently than others in a text, to design an algorithm which can represent the same piece of text using lesser number of bits. In variable length encoding, we assign variable number of bits to characters depending upon their frequency in the given text, so some characters might end up using only one bit.

## Prefix Free Code
The methodology used for Huffman coding results in a prefix-free code.  A prefix-free code is one in which the bit coding sequence representing some particular character is never a prefix of the bit coding sequence representing any other character.  For example, here is a possible bit sequence for a Huffman code on an alphabet with four characters where D is the most probable and A is the least probable:

## Algorithm

1. Create a leaf node for each character and add them to the priority queue.

2. While there is more than one node in the queue: <br />
    a. Remove the two nodes of lowest frequency from the queue.<br />
    b. Create a new internal node with these two nodes as children and with frequency equal to the sum of the two nodes' frequencies.<br />
    c. Add the new node to the priority queue.<br />
3. The remaining node is the root node and the tree is complete. 

<img src = "imag1.PNG" width = 650> 
<img src = "image2.PNG" width = 650> 
<img src = "image3.PNG" width = 650>
<img src = "image4.PNG" width = 400> 


# Examples
Example 1

<img src = "example1.PNG" width = 450> 

Example 2


<img src = "example2.PNG" width = 460> 
