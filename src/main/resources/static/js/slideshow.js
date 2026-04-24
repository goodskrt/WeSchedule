// Slideshow functionality for background images
document.addEventListener('DOMContentLoaded', function() {
    let slideIndex = 0;
    const slides = document.querySelectorAll('.slide-bg');
    
    function showSlides() {
        if (slides.length === 0) return;
        
        slides.forEach((slide) => {
            slide.classList.remove('active');
        });
        
        slideIndex++;
        if (slideIndex > slides.length) {
            slideIndex = 1;
        }
        
        slides[slideIndex - 1].classList.add('active');
        setTimeout(showSlides, 4000); // Change image every 4 seconds
    }
    
    // Start slideshow
    if (slides.length > 0) {
        showSlides();
    }
});
