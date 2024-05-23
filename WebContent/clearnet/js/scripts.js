/* Description: Custom JS file */


(function($) {
    "use strict"; 
    
    /* Particles */
	particlesJS("particles-js", {
		"particles": {
		"number": {
			"value": 120,
			"density": {
			"enable": true,
			"value_area": 1200
			}
		},
		"color": {
			"value": "#585f63"
		},
		"shape": {
			"type": "circle",
			"stroke": {
			"width": 0,
			"color": "#000000"
			},
			"polygon": {
			"nb_sides": 5
			},
			"image": {
			"src": "img/github.svg",
			"width": 100,
			"height": 100
			}
		},
		"opacity": {
			"value": 0.2,
			"random": false,
			"anim": {
			"enable": false,
			"speed": 1,
			"opacity_min": 0.2,
			"sync": false
			}
		},
		"size": {
			"value": 3,
			"random": true,
			"anim": {
			"enable": false,
			"speed": 40,
			"size_min": 0.1,
			"sync": false
			}
		},
		"line_linked": {
			"enable": true,
			"distance": 150,
			"color": "#585f63",
			"opacity": 0.4,
			"width": 1
		},
		"move": {
			"enable": true,
			"speed": 6,
			"direction": "none",
			"random": false,
			"straight": false,
			"out_mode": "out",
			"bounce": false,
			"attract": {
			"enable": false,
			"rotateX": 600,
			"rotateY": 1200
			}
		}
		},
		"interactivity": {
		"detect_on": "canvas",
		"events": {
			"onhover": {
			"enable": true,
			"mode": "grab"
			},
			"onclick": {
			"enable": true,
			"mode": "push"
			},
			"resize": true
		},
		"modes": {
			"grab": {
			"distance": 140,
			"line_linked": {
				"opacity": 1
			}
			},
			"bubble": {
			"distance": 400,
			"size": 40,
			"duration": 2,
			"opacity": 8,
			"speed": 3
			},
			"repulse": {
			"distance": 200,
			"duration": 0.4
			},
			"push": {
			"particles_nb": 4
			},
			"remove": {
			"particles_nb": 2
			}
		}
		},
		"retina_detect": true
    });
    

    /* Move Form Fields Label When User Types */
    // for input and textarea fields
    $("input, textarea").keyup(function(){
		if ($(this).val() != '') {
			$(this).addClass('notEmpty');
		} else {
			$(this).removeClass('notEmpty');
		}
    });


	/* Removes Long Focus On Buttons */
	$(".button, a, button").mouseup(function() {
		$(this).blur();
	});

})(jQuery);