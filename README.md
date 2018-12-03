Start with arguments to choose which game and which training algorithm to use:

for example: java -jar pws.jar game=flappybird ai=ga 
will launch with flappybird and the geneticalgorithm as training algorithm.
     
Arguments can be:
game    With one of following values xor, flappy_bird, flappybird and pong (default=pong)
width   With a positive integer value (default=800)
height  With a positive integer value (default=800)
ai with With one of following values pool, ga, geneticalgorithm, nn, neuralnetwork (default=nn)
ai_type same as ai
layers  With an list of numbers in the following format: a,b,c (Numbers with a , as divider)